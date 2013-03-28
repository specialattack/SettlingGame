
package net.specialattack.settling.launcher;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.lwjgl.LWJGLUtil;

public class LauncherFrame extends Frame implements Runnable {

    private static final long serialVersionUID = 5344282572136619990L;
    public static LauncherFrame instance;
    private LauncherStub launcher;
    private Applet applet;
    private BufferedImage menuImage;
    private BufferedImage globe;
    private BufferedImage playNormal;
    private BufferedImage playHover;
    private BufferedImage quitNormal;
    private BufferedImage quitHover;
    private BufferedImage title;
    private float rot = 0;
    private InputHandler input;
    private boolean running = true;

    public LauncherFrame(LauncherStub launcher) {
        super("Settling Launcher Window");

        this.launcher = launcher;
        LauncherFrame.instance = this;
        this.setBackground(Color.black);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        p.setPreferredSize(new Dimension(854, 480));

        this.setLayout(new BorderLayout());
        this.add(p, "Center");

        this.pack();
        this.setLocationRelativeTo(null);

        if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS) {
            this.addWindowFocusListener(new WindowAdapter() {

                @Override
                public void windowGainedFocus(WindowEvent arg0) {
                    if (LauncherFrame.this.applet != null) {
                        LauncherFrame.this.applet.requestFocusInWindow();
                    }
                }
            });
        }

        this.input = new InputHandler();
        this.addMouseListener(this.input);
        this.addMouseMotionListener(this.input);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(30000L);
                        }
                        catch (InterruptedException e) {}
                        System.out.println("FORCING EXIT!");
                        System.exit(0);
                    }
                }.start();

                System.out.println("Closing window...");

                if (LauncherFrame.this.applet != null) {
                    LauncherFrame.this.applet.stop();
                    System.exit(0);
                }
                else {
                    System.exit(0);
                }
            }
        });
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            this.menuImage = ImageIO.read(LauncherFrame.class.getResource("/launcherbg.png"));
            this.globe = ImageIO.read(LauncherFrame.class.getResource("/globe.png"));
            this.playNormal = ImageIO.read(LauncherFrame.class.getResource("/Play_normal.png"));
            this.playHover = ImageIO.read(LauncherFrame.class.getResource("/Play_hover.png"));
            this.quitNormal = ImageIO.read(LauncherFrame.class.getResource("/Quit_normal.png"));
            this.quitHover = ImageIO.read(LauncherFrame.class.getResource("/Quit_hover.png"));
            this.title = ImageIO.read(LauncherFrame.class.getResource("/title.png"));
        }
        catch (IOException e1) {
            System.out.println("Failed to load Images");
            System.exit(0);
        }
        int returnCode = -1;
        while (this.running) {

            try {
                this.rot += 0.002F;
                returnCode = this.renderLauncher();
                this.tickLauncher();
                Thread.sleep(1L);
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
            if (returnCode > -1) {
                break;
            }
        }
        if (returnCode == 0) {
            this.launchClicked();
        }

        if (returnCode == 1) {
            this.dispose();
            System.exit(0);
        }

    }

    public int renderLauncher() throws IOException {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return -1;
        }

        Graphics g = bs.getDrawGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        {
            double rotationRequired = Math.toRadians(this.rot);
            AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, this.globe.getWidth() / 2, this.globe.getHeight() / 2);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            // Drawing the rotated image at the required drawing locations
            g.drawImage(op.filter(this.globe, null), 30 - this.globe.getWidth() / 2, (this.getHeight() - this.globe.getHeight()) / 2, null);
        }

        g.drawImage(this.title, (this.getWidth() - this.title.getWidth()) / 2 + 210, 100, null);

        if (InputHandler.MouseX > 854 - 250 && InputHandler.MouseX < 854 - 250 + this.playNormal.getWidth() && InputHandler.MouseY > this.getHeight() / 2 - 20 && InputHandler.MouseY < this.playNormal.getHeight() + this.getHeight() / 2 - 20) {
            g.drawImage(this.playHover, 854 - 250, this.getHeight() / 2 - 20, null);
            if (InputHandler.MouseButton == 1) {
                this.running = false;
                return 0;
            }
        }
        else {
            g.drawImage(this.playNormal, 854 - 250, this.getHeight() / 2 - 20, null);
        }

        if (InputHandler.MouseX > 854 - 250 && InputHandler.MouseX < 854 - 250 + this.quitNormal.getWidth() && InputHandler.MouseY > this.getHeight() / 2 + 20 && InputHandler.MouseY < this.quitNormal.getHeight() + this.getHeight() / 2 + 20) {
            g.drawImage(this.quitHover, 854 - 250, this.getHeight() / 2 + 20, null);
            if (InputHandler.MouseButton == 1) {
                this.running = false;
                return 1;
            }
        }
        else {
            g.drawImage(this.quitNormal, 854 - 250, this.getHeight() / 2 + 20, null);
        }

        g.dispose();
        bs.show();
        return -1;
    }

    public void tickLauncher() {

    }

    private void launchClicked() {
        Applet applet = LauncherFrame.this.launcher.launchGame();
        LauncherFrame.this.applet = LauncherFrame.this.launcher;
        LauncherFrame.this.launcher.setApplet(applet);
        LauncherFrame.this.initiateApplet();
        LauncherFrame.this.setTitle("Settling");
    }

    private void initiateApplet() {
        Component component = this.getComponent(0);

        this.removeAll();

        this.applet.setPreferredSize(new Dimension(component.getWidth(), component.getHeight()));

        this.setLayout(new BorderLayout());
        this.add(this.applet, "Center");

        this.pack();

        this.applet.init();
        this.applet.start();
    }

    public static void main(String[] args, LauncherStub launcher) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception localException) {}

        LauncherFrame frame = new LauncherFrame(launcher);
        frame.setVisible(true);
        frame.start();
    }

}
