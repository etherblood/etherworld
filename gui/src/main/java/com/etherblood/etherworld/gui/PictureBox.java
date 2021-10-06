package com.etherblood.etherworld.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serial;
import javax.swing.JPanel;

public class PictureBox extends JPanel {

    @Serial
    private static final long serialVersionUID = 9162749575030271712L;
    private BufferedImage image = null;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    public BufferedImage createImage() {
        return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }
}
