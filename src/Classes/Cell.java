/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Ikikay
 */
public class Cell {

    int xPos;
    int yPos;
    boolean isVirgin = true;
    boolean isBomb = false;
    boolean isBorder = false;
    boolean isScanned = false;
    int width = 15;
    int height = 16;
    int nbr = 0;
    Point coinTopLeft;

    public Cell() {
    }

    public Cell(Point pointTopLeft) {
        coinTopLeft = pointTopLeft;
    }

    public Rectangle getRectangle(int width, int height) {
        return new Rectangle(coinTopLeft.x, coinTopLeft.y, width, height);
    }
    
    public Point getEndPoint(){
        return new Point((coinTopLeft.x + width), (coinTopLeft.y + height));
    }

    public int getxPos() {
        return xPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

    public boolean isIsVirgin() {
        return isVirgin;
    }

    public void setIsVirgin(boolean isVirgin) {
        this.isVirgin = isVirgin;
    }

    public boolean isIsBomb() {
        return isBomb;
    }

    public void setIsBomb(boolean isBomb) {
        this.isBomb = isBomb;
    }

    public boolean isIsBorder() {
        return isBorder;
    }

    public void setIsBorder(boolean isBorder) {
        this.isBorder = isBorder;
    }

    public boolean isIsScanned() {
        return isScanned;
    }

    public void setIsScanned(boolean isScanned) {
        this.isScanned = isScanned;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getNbr() {
        return nbr;
    }

    public void setNbr(int nbr) {
        this.nbr = nbr;
    }

    public Point getCoinTopLeft() {
        return coinTopLeft;
    }

    public void setCoinTopLeft(Point coinTopLeft) {
        this.coinTopLeft = coinTopLeft;
    }

}
