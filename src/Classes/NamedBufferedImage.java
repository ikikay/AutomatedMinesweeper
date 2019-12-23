/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.awt.image.BufferedImage;


/**
 *
 * @author lulambert
 */
public class NamedBufferedImage {

    private BufferedImage image;
    private String name;

    public NamedBufferedImage(BufferedImage image, String name) {
        this.image = image;
        this.name = name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
