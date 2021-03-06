
package net.specialattack.settling.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.JPanel;

import net.specialattack.settling.client.crash.CrashReportSectionCamera;
import net.specialattack.settling.client.gui.GuiScreen;
import net.specialattack.settling.client.gui.GuiScreenMainMenu;
import net.specialattack.settling.client.gui.GuiScreenMenu;
import net.specialattack.settling.client.rendering.ChunkRenderer;
import net.specialattack.settling.client.rendering.FontRenderer;
import net.specialattack.settling.client.shaders.Shader;
import net.specialattack.settling.client.shaders.ShaderLoader;
import net.specialattack.settling.client.texture.TextureRegistry;
import net.specialattack.settling.client.util.KeyBinding;
import net.specialattack.settling.client.util.ScreenResolution;
import net.specialattack.settling.client.util.Settings;
import net.specialattack.settling.client.util.camera.ICamera;
import net.specialattack.settling.client.util.camera.OverviewCamera;
import net.specialattack.settling.common.Settling;
import net.specialattack.settling.common.crash.CrashReport;
import net.specialattack.settling.common.crash.CrashReportSectionThrown;
import net.specialattack.settling.common.lang.LanguageRegistry;
import net.specialattack.settling.common.util.Location;
import net.specialattack.settling.common.world.Chunk;
import net.specialattack.settling.common.world.World;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

public class SettlingClient extends Settling {

    public static SettlingClient instance;
    private Canvas canvas;
    private int displayWidth;
    private int displayHeight;
    private Shader shader;
    public World currentWorld = null;
    public FontRenderer fontRenderer;
    private HashMap<Chunk, ChunkRenderer> chunkList;
    private ArrayList<Chunk> dirtyChunks;
    private ArrayList<ChunkRenderer> renderedChunks;
    private int fps;
    private int tps;
    private GuiScreen currentScreen = null;
    private boolean fullscreen = false;
    public ICamera camera;

    public SettlingClient() {
        instance = this;
    }

    public void setFullscreen(boolean state) {
        if (!this.fullscreen && state) {
            try {
                DisplayMode mode = Settings.displayMode.getMode();

                Display.setDisplayModeAndFullscreen(mode);

                this.resize(mode.getWidth(), mode.getHeight());

                this.fullscreen = true;
            }
            catch (LWJGLException e) {
                Settling.log.log(Level.SEVERE, "Failed enabling fullscreen mode", e);

                this.setFullscreen(false);
            }
        }
        else if (this.fullscreen && !state) {
            try {
                Display.setFullscreen(false);

                this.resize(this.canvas.getWidth(), this.canvas.getHeight());

                this.fullscreen = false;
            }
            catch (LWJGLException e) {
                Settling.log.log(Level.SEVERE, "Failed disabling fullscreen mode", e);
            }
        }
    }

    public void updateFullscreen() {
        if (this.fullscreen) {
            try {
                DisplayMode mode = Settings.displayMode.getMode();

                Display.setDisplayModeAndFullscreen(mode);

                this.resize(mode.getWidth(), mode.getHeight());
            }
            catch (LWJGLException e) {
                Settling.log.log(Level.SEVERE, "Failed updating fullscreen mode", e);
            }

        }
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        canvas.requestFocusInWindow();
    }

    public void resize(int width, int height) {
        this.displayWidth = width <= 0 ? 1 : width;
        this.displayHeight = height <= 0 ? 1 : height;

        if (this.currentScreen != null) {
            this.currentScreen.resize(width, height);
        }

        GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, this.displayWidth, this.displayHeight, 0.0D, 1000.0D, -1000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void displayScreen(GuiScreen screen) {
        this.currentScreen = screen;

        if (this.currentScreen != null) {
            this.currentScreen.initialize(this.displayWidth, this.displayHeight);
        }

        if (screen == null && this.currentWorld == null) {
            this.displayScreen(new GuiScreenMainMenu());
        }
    }

    private void setupDisplay() throws LWJGLException {
        PixelFormat format = new PixelFormat();
        Display.setParent(this.canvas);
        Display.create(format);
        ScreenResolution.initialize();

        this.displayWidth = Display.getWidth();
        this.displayHeight = Display.getHeight();

    }

    @Override
    protected boolean startup() {
        try {
            this.setupDisplay();
        }
        catch (LWJGLException e) {
            Settling.log.log(Level.SEVERE, "Failed starting Settling", e);
            return false;
        }

        // TODO: Move this stuff over

        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, this.displayWidth, this.displayHeight, 0.0D, 1000.0D, -1000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        // GL11.glEnable(GL11.GL_TEXTURE_2D);

        try {
            Mouse.create();
        }
        catch (LWJGLException e) {
            Settling.log.log(Level.SEVERE, "Failed starting Settling", e);
            return false;
        }

        this.shader = ShaderLoader.createShader("grayscale");

        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        if (this.shader != null) {
            this.shader.bindShader();

            TextureRegistry.tiles.bindTexture();

            int loc = GL20.glGetUniformLocation(this.shader.programId, "texture1");
            GL20.glUniform1i(loc, 0);
        }

        Shader.unbindShader();

        LanguageRegistry.loadLang("en_US");
        Settings.loadSettings();

        if (Settings.fullscreen.getState()) {
            this.setFullscreen(true);
        }

        // this.currentWorld = new WorldDemo(new File("./demo/"));

        this.fontRenderer = new FontRenderer();

        this.chunkList = new HashMap<Chunk, ChunkRenderer>();
        this.dirtyChunks = new ArrayList<Chunk>();
        this.renderedChunks = new ArrayList<ChunkRenderer>();

        this.camera = new OverviewCamera();

        // TODO: move over
        BufferedImage image = TextureRegistry.openResource("/textures/tiles/water.png");
        TextureRegistry.tiles.loadTexture(image, "water", image.getWidth(), image.getHeight());
        image = TextureRegistry.openResource("/textures/tiles/sand.png");
        TextureRegistry.tiles.loadTexture(image, "sand", image.getWidth(), image.getHeight());
        image = TextureRegistry.openResource("/textures/tiles/grass.png");
        TextureRegistry.tiles.loadTexture(image, "grass", image.getWidth(), image.getHeight());

        this.displayScreen(new GuiScreenMainMenu());

        return true;
    }

    @Override
    protected void shutdown() {
        Display.destroy();

        System.exit(0);
    }

    @Override
    protected void runGameLoop() {
        int frames = 0;
        int ticks = 0;
        long lastTimer1 = System.currentTimeMillis();
        while (this.isRunning() && !this.isShuttingDown()) {
            if (Display.isCloseRequested()) {
                this.attemptShutdown();
            }

            if (this.currentScreen != null) {
                float partialTicks = this.timer.renderPartialTicks;
                this.timer.update();
                this.timer.renderPartialTicks = partialTicks;
            }
            else {
                this.timer.update();
            }

            for (int i = 0; i < this.timer.remainingTicks; i++) {
                ticks++;
                this.tick();
            }

            while (this.dirtyChunks.size() > 0) {
                this.updateChunks(); // FIXME: debug code
            }

            frames++;
            this.render();
            Display.update();

            Mouse.poll();
            Keyboard.poll();

            while (Mouse.next()) {
                int dWheel = Mouse.getDWheel();
                dWheel = dWheel > 0 ? 1 : dWheel < 0 ? -1 : 0;
                if (Mouse.getEventButton() != -1 && this.currentScreen != null && Mouse.isButtonDown(Mouse.getEventButton())) {
                    this.currentScreen.mousePressed(Mouse.getEventButton(), Mouse.getX(), this.displayHeight - 1 - Mouse.getY());
                }
                else if (Mouse.hasWheel() && dWheel != 0 && this.currentScreen != null) {
                    this.currentScreen.mouseScrolled(dWheel);
                }
            }

            while (Keyboard.next()) {
                if (Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
                    if (this.currentScreen != null) {
                        this.currentScreen.keyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter());
                    }
                }
            }

            Display.sync(60);

            if (System.currentTimeMillis() - lastTimer1 > 1000) {
                lastTimer1 += 1000;
                // System.out.println(ticks + " ticks - " + frames + " fps");
                this.fps = frames;
                this.tps = ticks;
                frames = 0;
                ticks = 0;
            }
        }
    }

    @Override
    public void handleError(Throwable thrown) {
        this.attemptShutdownCrash();

        GL11.glReadBuffer(GL11.GL_FRONT);
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(this.displayWidth * this.displayHeight * bpp);
        GL11.glReadPixels(0, 0, this.displayWidth, this.displayHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        final BufferedImage image = new BufferedImage(this.displayWidth, this.displayHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < this.displayWidth; x++) {
            for (int y = 0; y < this.displayHeight; y++) {
                int i = (x + (this.displayWidth * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, this.displayHeight - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        Display.destroy();

        Frame frame = new Frame("Settling Crash");

        TextArea text = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        text.setBackground(Color.WHITE);

        CrashReport report = new CrashReport();
        report.addSection(new CrashReportSectionThrown(thrown));

        if (this.currentWorld != null) {
            report.addSection(new CrashReportSectionCamera(this.camera, "Active Camera"));
        }

        text.setText(report.getData());

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(854, 480));
        panel.setLayout(new BorderLayout());
        panel.add(text, "Center");
        frame.setLayout(new BorderLayout());
        frame.add(panel, "Center");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });
        frame.setVisible(true);

        JPanel imgPanel = new JPanel() {
            private static final long serialVersionUID = -8793690198628860722L;

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        };
        imgPanel.setLayout(new BorderLayout());

        SettlingApplet.instance.display(imgPanel);
    }

    private void tick() {
        if (this.currentScreen == null && this.currentWorld != null) {
            this.camera.tick(this.currentWorld, this);
        }

        KeyBinding.escape.update();

        Settings.update();

        boolean escapeTapped = KeyBinding.escape.isTapped();

        if (this.currentScreen != null && this.currentWorld != null && escapeTapped) {
            this.displayScreen(null);
        }
        else if (this.currentWorld != null && escapeTapped) {
            this.displayScreen(new GuiScreenMenu());
        }
    }

    private void render() {
        if (!this.fullscreen && (this.displayWidth != this.canvas.getWidth() || this.displayHeight != this.canvas.getHeight())) {
            this.resize(this.canvas.getWidth(), this.canvas.getHeight());
        }
        float scale = 1F;
        GL11.glScalef(scale, scale, scale);
        this.clearGL();
        this.render3D();
        this.render2D();
    }

    private void render2D() {
        this.initGL2();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (this.currentScreen != null) {
            this.currentScreen.render(Mouse.getX(), this.displayHeight - 1 - Mouse.getY());
        }

        this.fontRenderer.renderStringWithShadow("Settling pre-alpha 0.1", 0, 2, 0xFFFFFFFF);
        this.fontRenderer.renderStringWithShadow("FPS: " + this.fps + " TPS: " + this.tps, 0, 18, 0xFFFFFFFF);
        Location loc = this.camera.getLocation();
        this.fontRenderer.renderStringWithShadow("Yaw: " + loc.yaw + " Pitch: " + loc.pitch, 0, 34, 0xFFFFFFFF);
        this.fontRenderer.renderStringWithShadow("X: " + loc.x + " Y: " + loc.y + " Z: " + loc.z, 0, 50, 0xFFFFFFFF);
        this.fontRenderer.renderStringWithShadow("Dirty Chunks: " + this.dirtyChunks.size(), 0, 66, 0xFFFFFFFF);
        this.fontRenderer.renderStringWithShadow("Rendered Chunks: " + this.renderedChunks.size(), 0, 84, 0xFFFFFFFF);

        this.fontRenderer.renderStringWithShadow("Raw Time: " + (this.timer.totalTicks / 1) % 24000, 0, 100, 0xFFFFFFFF);

        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        String usedString = "Used memory: " + usedMemory * 100L / maxMemory + "% (" + usedMemory / 1024L / 1024L + "MB of " + maxMemory / 1024L / 1024L + "MB)";
        String allocatedString = "Allocated memory: " + totalMemory * 100L / maxMemory + "% (" + totalMemory / 1024L / 1024L + "MB)";
        this.fontRenderer.renderStringWithShadow(usedString, this.displayWidth - this.fontRenderer.getStringWidth(usedString) - 1, 0, 0xFFFFFFFF);
        this.fontRenderer.renderStringWithShadow(allocatedString, this.displayWidth - this.fontRenderer.getStringWidth(allocatedString) - 1, 16, 0xFFFFFFFF);
    }

    private void render3D() {
        if (this.currentWorld == null) {
            return;
        }

        this.initGL3();
        GL11.glPushMatrix();

        this.camera.lookThrough(this.timer.renderPartialTicks);

        // This would go to the world/level class

        this.levelRender();

        GL11.glPopMatrix();
    }

    // Our 3d rendering
    public void initGL3() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GLU.gluPerspective(this.camera.getFOV(this.timer.renderPartialTicks), (float) this.displayWidth / (float) this.displayHeight, 0.5F, 2000.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.5F);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        // GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    }

    // 2d rendering (For GUI and stuff)
    public void initGL2() {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, this.displayWidth, this.displayHeight, 0.0D, -1.0D, 1.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // Clear the canvas
    private void clearGL() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        // GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLoadIdentity();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void updateChunks() {
        if (this.dirtyChunks.size() > 0) {
            double distance = -1.0F;
            Chunk toRender = null;

            for (Chunk chunk : this.dirtyChunks) {
                double cDistance = (-this.camera.getLocation().x / 16.0F + 0.5F - (float) chunk.chunkX) * (-this.camera.getLocation().x / 16.0F + 0.5F - (float) chunk.chunkX);
                cDistance += (-this.camera.getLocation().z / 16.0F + 0.5F - (float) chunk.chunkZ) * (-this.camera.getLocation().z / 16.0F + 0.5F - (float) chunk.chunkZ);

                if (cDistance < distance || distance < 0) {
                    distance = cDistance;
                    toRender = chunk;
                }
            }

            if (toRender != null) {
                this.dirtyChunks.remove(toRender);

                ChunkRenderer chunkRenderer;
                boolean isRenderedAlready = false;

                if (this.chunkList.containsKey(toRender)) {
                    chunkRenderer = this.chunkList.get(toRender);
                    isRenderedAlready = !chunkRenderer.dirty;
                }
                else {
                    chunkRenderer = new ChunkRenderer(toRender);
                }

                chunkRenderer.createGlChunk();

                if (!isRenderedAlready) {
                    this.chunkList.put(toRender, chunkRenderer);
                    this.renderedChunks.add(chunkRenderer);
                }
            }
        }
    }

    @Deprecated
    public void markChunksDirty() {
        this.dirtyChunks.clear();
        this.chunkList.clear();
        this.renderedChunks.clear();

        if (this.currentWorld != null) {
            for (int x = this.currentWorld.getMinChunkXBorder(); x < this.currentWorld.getMaxChunkXBorder(); x++) {
                for (int z = this.currentWorld.getMinChunkZBorder(); z < this.currentWorld.getMaxChunkZBorder(); z++) {
                    Chunk chunk = this.currentWorld.getChunk(x, z, true);

                    if (chunk != null) {
                        this.dirtyChunks.add(chunk);
                    }

                }
            }
        }
    }

    FloatBuffer matSpecular;
    FloatBuffer lightPosition;
    FloatBuffer whiteLight;
    FloatBuffer lModelAmbient;

    private void initLightArrays() {

        //Brightness?
        long tickCount = this.timer.totalTicks;
        long timeRaw = (tickCount / 1) % 24000;
        //timeRaw = 0;

        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        if (timeRaw > 12000 && timeRaw <= 14000) {
            red = 1.0F - 0.8F * (timeRaw - 12000.0F) / 2000.0F;
            green = 1.0F - 0.7F * (timeRaw - 12000.0F) / 2000.0F;
        }
        else if (timeRaw > 14000 && timeRaw <= 22000) {
            red = 0.2F;
            green = 0.3F;
        }
        else if (timeRaw > 22000) {
            red = 0.2F + 0.8F * (((float) timeRaw - 22000.0F) / 2000.0F);
            green = 0.3F + 0.7F * (((float) timeRaw - 22000.0F) / 2000.0F);
        }

        this.matSpecular = BufferUtils.createFloatBuffer(4);
        this.matSpecular.put(1.0F).put(1.0F).put(1.0F).put(1.0F).flip();

        this.lightPosition = BufferUtils.createFloatBuffer(4);
        this.lightPosition.put(1.0F).put(1.0F).put(1.0F).put(0.0F).flip();

        this.whiteLight = BufferUtils.createFloatBuffer(4);
        this.whiteLight.put(red).put(green).put(blue).put(1.0F).flip();

        this.lModelAmbient = BufferUtils.createFloatBuffer(4);
        this.lModelAmbient.put(red).put(green).put(blue).put(1.0F).flip();
    }

    private void levelRender() {
        TextureRegistry.tiles.bindTexture();

        //Marshal, Lights!
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        this.initLightArrays();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, this.matSpecular); // sets specular material color
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 50.0F); // sets shininess

        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, this.lightPosition); // sets light position
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, this.whiteLight); // sets specular light to white
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, this.whiteLight); // sets diffuse light to white
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.lModelAmbient); // global ambient light 

        GL11.glEnable(GL11.GL_LIGHTING); // enables lighting
        GL11.glEnable(GL11.GL_LIGHT0); // enables light0

        GL11.glEnable(GL11.GL_COLOR_MATERIAL); // enables opengl to use glColor3f to define material color
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);

        int renderChunkRadius = 16;
        for (ChunkRenderer chunkRenderer : this.renderedChunks) {
            double distance = (-this.camera.getLocation().x / 16.0F + 0.5F - (float) chunkRenderer.chunk.chunkX) * (-this.camera.getLocation().x / 16.0F + 0.5F - (float) chunkRenderer.chunk.chunkX);
            distance += (-this.camera.getLocation().z / 16.0F + 0.5F - (float) chunkRenderer.chunk.chunkZ) * (-this.camera.getLocation().z / 16.0F + 0.5F - (float) chunkRenderer.chunk.chunkZ);

            if (distance < renderChunkRadius * renderChunkRadius) {
                chunkRenderer.renderChunk();
            }
        }
        GL11.glDisable(GL11.GL_LIGHTING);
    }

}
