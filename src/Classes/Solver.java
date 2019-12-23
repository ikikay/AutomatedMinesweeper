/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import static AutomatedMinesweeper.AutomatedMinesweeper.margeLeft;
import static AutomatedMinesweeper.AutomatedMinesweeper.margeTop;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Ikikay
 */
public class Solver extends Thread {

    protected volatile boolean running = true;
    public Rectangle overlayArea;
    public Point beginGamePosition;
    public Point endGamePosition;
    public int cellWidth = 15;
    public int cellHeight = 16;
    int nbrCellsX;
    int nbrCellsY;

    Cell[][] gameArea;

    int grisHorsGrille = -14671325;
    int grisContour = -12566464;
    int blancContour = -394759;
    int grisDedans = -2434342;
    int colorUn = -13402626;
    int colorDeux = -8454530;
    int colorTrois = -117965;
    int colorQuatre = -8541186;
    int colorCinq = -0;
    int colorSix = -0;
    int colorSept = -0;
    int colorHuit = -0;

    int[][] layoutCoinTopLeft = {
        {grisHorsGrille, grisHorsGrille, grisHorsGrille, grisHorsGrille},
        {grisHorsGrille, grisContour, grisContour, grisContour},
        {grisHorsGrille, grisContour, blancContour, blancContour},
        {grisHorsGrille, grisContour, blancContour, grisDedans}
    };
    int[][] layoutCoinBotRight = {
        {grisDedans, grisDedans, grisContour, grisHorsGrille},
        {grisDedans, grisDedans, grisContour, grisHorsGrille},
        {grisContour, grisContour, grisContour, grisHorsGrille},
        {grisHorsGrille, grisHorsGrille, grisHorsGrille, grisHorsGrille}
    };

    public Solver(int x, int y, int width, int height) {
        this.overlayArea = new Rectangle(x, y, width, height);
    }

    public void close() {
        System.out.println("Demande d'éxtinction");

        running = false;
    }

    public void run() {
        while (running) {
            Chrono chrono = new Chrono();
            chrono.start();

            detectGameArea();
            beginGame();
            detectCells();

            while (!isFinish()) {
                findLogicalBomb();
                discoverNewCells();
                detectCells();
                afficheTableau();
            }

            /*
            try {
                BufferedImage test = ImageIO.read(new File("6.bmp"));
                analyseRGBImage(test);
            } catch (IOException ex) {
                Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
            }
            //afficheTableau();
             */
            close();

            chrono.stop();
            chrono.printSec();
            //System.exit(0);
        }
    }

    public void detectGameArea() {
        try {
            System.out.println("Screen de l'overlay, pour recherche de la zone de jeux");
            Robot robot = new Robot();
            BufferedImage capturedOverlayArea = robot.createScreenCapture(new Rectangle(this.overlayArea));

            beginGamePosition = searchExactly(capturedOverlayArea, layoutCoinTopLeft, "coinTopLeft", new Point(0, 0), new Point(capturedOverlayArea.getWidth(), capturedOverlayArea.getHeight()));
            beginGamePosition.x = beginGamePosition.x + 2;                      // 2 pour supprimer le contour et l'exterieur
            beginGamePosition.y = beginGamePosition.y + 2;                      // 2 pour supprimer le contour et l'exterieur
            endGamePosition = searchExactly(capturedOverlayArea, layoutCoinBotRight, "coinBotRight", new Point(0, 0), new Point(capturedOverlayArea.getWidth(), capturedOverlayArea.getHeight()));
            endGamePosition.x = endGamePosition.x + layoutCoinBotRight.length - 1;// 1 pour supprimer l'exterieur
            endGamePosition.y = endGamePosition.y + layoutCoinBotRight.length - 1;// 1 pour supprimer l'exterieur

            if ((beginGamePosition.getX() != 0) && (endGamePosition.getY() != 0)) {
                int gameAreaWidth = endGamePosition.x - beginGamePosition.x;
                int gameAreaHeight = endGamePosition.y - beginGamePosition.y;
                nbrCellsX = ((gameAreaWidth - (cellWidth - 1)) / cellWidth) + 1;    // La première cellule fait 1 pixel de moins
                nbrCellsY = gameAreaHeight / cellHeight;

                System.out.println("Jeux de : " + nbrCellsX + "x" + nbrCellsY);
                gameArea = new Cell[nbrCellsY + 2][nbrCellsX + 2];
                assignCells();
            } else {
                System.out.println("Impossible de trouver la zone de jeux");
                close();
            }

            System.out.println("");
        } catch (AWTException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void assignCells() {
        System.out.println("");
        System.out.println("Assignation de la matrice de jeux");

        for (int y = 0; y < gameArea.length; y++) {
            for (int x = 0; x < gameArea[y].length; x++) {
                //System.out.print(x + "," + y);
                //Action sur gameArea[y][x]
                Point cellTopLeft = new Point();

                cellTopLeft.x = (beginGamePosition.x - cellWidth) + (x * cellWidth);
                cellTopLeft.y = (beginGamePosition.y - cellHeight) + (y * cellHeight);

                gameArea[y][x] = new Cell(cellTopLeft);
                gameArea[y][x].setxPos(x);
                gameArea[y][x].setyPos(y);

                if (x == 0) {
                    gameArea[y][x].isBorder = true;
                    gameArea[y][x].isVirgin = false;
                } else if (y == 0) {
                    gameArea[y][x].isBorder = true;
                    gameArea[y][x].isVirgin = false;
                } else if (x == 1) {
                    gameArea[y][x].setWidth(14);
                    gameArea[y][x].setCoinTopLeft(cellTopLeft);
                }

                if (x >= 2) {
                    cellTopLeft.x -= 1;
                    gameArea[y][x].setCoinTopLeft(cellTopLeft);
                }

                if (x == (gameArea[y].length - 1)) {
                    gameArea[y][x].isBorder = true;
                    gameArea[y][x].isVirgin = false;
                } else if (y == (gameArea.length - 1)) {
                    gameArea[y][x].isBorder = true;
                    gameArea[y][x].isVirgin = false;
                }

                System.out.print(cellTopLeft.getX() + "," + cellTopLeft.getY());
                System.out.print(" ; ");
            }
            System.out.println("");
        }
    }

    public void detectCells() {
        try {
            Robot robot = new Robot();
            System.out.println("Screen de l'overlay, pour annalyse des cellules");
            BufferedImage capturedOverlayArea = robot.createScreenCapture(new Rectangle(this.overlayArea));

            for (int y = 0; y < gameArea.length; y++) {
                for (int x = 0; x < gameArea[y].length; x++) {
                    //Action sur gameArea[y][x]
                    analyseOfCell(capturedOverlayArea, gameArea[y][x]);
                    //System.out.println("");
                }
                //System.out.println("");

            }
        } catch (AWTException ex) {
            Logger.getLogger(Solver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void analyseOfCell(BufferedImage capturedOverlayArea, Cell cellToAnalyse) {
        if (!cellToAnalyse.isScanned) {
            int[][] layoutVirgin = {
                {blancContour, blancContour, blancContour, blancContour, blancContour, blancContour, blancContour, blancContour},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans},
                {blancContour, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans, grisDedans}
            };
            int[][] layoutUn = {
                {-15057731, -13402626, -13411209},
                {-15057731, -13402626, -13411209},
                {-15057731, -13402626, -13411209}
            };
            int[][] layoutDeux = {
                {-13919378, -8454530, -8454563},
                {-10682754, -8454530, -8454530},
                {-8454530, -8454530, -8454530}
            };
            int[][] layoutTrois = {
                {-117965, -120285},
                {-2215117, -117973}
            };
            int[][] layoutQuatre = {
                {-13932578, -8541186, -10793187},
                {-8541186, -8541186, -8541186},
                {-8541186, -8541186, -8541186}
            };
            int[][] layoutCinq = {
                {-98947, -98947},
                {-98947, -98947}
            };
            int[][] layoutSix = {
                {-8520450, -8520450},
                {-8520450, -8520450}
            };
            int[][] layoutSept = {{colorSept}};
            int[][] layoutHuit = {{colorHuit}};

            Point findedPosition = searchExactly(capturedOverlayArea, layoutVirgin, "Cellule vierge", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
            if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                cellToAnalyse.setIsVirgin(true);
            } else {
                findedPosition = searchExactly(capturedOverlayArea, layoutUn, "Numéro 1", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                    cellToAnalyse.setNbr(1);
                    cellToAnalyse.setIsVirgin(false);
                } else {
                    findedPosition = searchExactly(capturedOverlayArea, layoutDeux, "Numéro 2", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                    if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                        cellToAnalyse.setNbr(2);
                        cellToAnalyse.setIsVirgin(false);
                    } else {
                        findedPosition = searchExactly(capturedOverlayArea, layoutTrois, "Numéro 3", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                        if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                            cellToAnalyse.setNbr(3);
                            cellToAnalyse.setIsVirgin(false);
                        } else {
                            findedPosition = searchExactly(capturedOverlayArea, layoutQuatre, "Numéro 4", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                            if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                                cellToAnalyse.setNbr(4);
                                cellToAnalyse.setIsVirgin(false);
                            } else {
                                findedPosition = searchExactly(capturedOverlayArea, layoutCinq, "Numéro 5", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                                if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                                    cellToAnalyse.setNbr(5);
                                    cellToAnalyse.setIsVirgin(false);
                                } else {
                                    findedPosition = searchExactly(capturedOverlayArea, layoutSix, "Numéro 6", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                                    if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                                        cellToAnalyse.setNbr(6);
                                        cellToAnalyse.setIsVirgin(false);
                                    } else {
                                        findedPosition = searchExactly(capturedOverlayArea, layoutSept, "Numéro 7", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                                        if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                                            cellToAnalyse.setNbr(7);
                                            cellToAnalyse.setIsVirgin(false);
                                        } else {
                                            findedPosition = searchExactly(capturedOverlayArea, layoutHuit, "Numéro 8", cellToAnalyse.getCoinTopLeft(), cellToAnalyse.getEndPoint());
                                            if ((findedPosition.x != 0) && (findedPosition.y != 0)) {
                                                cellToAnalyse.setNbr(8);
                                                cellToAnalyse.setIsVirgin(false);
                                            } else {
                                                cellToAnalyse.setNbr(0);
                                                cellToAnalyse.setIsVirgin(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!cellToAnalyse.isVirgin) {
            cellToAnalyse.setIsScanned(true);
        }

    }

    public boolean isFinish() {
        boolean finish = true;
        for (int y = 0; y < gameArea.length; y++) {
            for (int x = 0; x < gameArea[y].length; x++) {
                //Action sur gameArea[y][x]
                if (gameArea[y][x].isVirgin) {
                    finish = false;
                }
            }
        }
        return finish;
    }

    public void beginGame() {
        try {
            System.out.println("Début de la partie, clique aléatoire sur la zone de jeux");

            System.out.println("Recherche de x entre : 1 et " + nbrCellsX);
            double xRandom = (Math.random() * (nbrCellsX) + 1);
            //double xRandom = 1;
            System.out.println("Recherche de y entre : 1 et " + nbrCellsY);
            double yRandom = (Math.random() * (nbrCellsY) + 1);
            //double yRandom = 1;

            Robot robot = new Robot();

            System.out.println("Clique en : " + (int) xRandom + "," + (int) yRandom);
            robot.mouseMove(
                    (beginGamePosition.x + (((int) xRandom - 1) * cellWidth) + (int) (cellWidth / 2) - 1),
                    (beginGamePosition.y + (((int) yRandom - 1) * cellHeight) + (int) (cellHeight / 2) - 1)
            );
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);                     // Clique gauche
            this.sleep(100);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);                   // Relache le clique gauche

            System.out.println("");

        } catch (AWTException ex) {
            Logger.getLogger(Solver.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (InterruptedException ex) {
            Logger.getLogger(Solver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean findLogicalBomb() {
        boolean blocked = true;
        for (int y = 0; y < gameArea.length; y++) {
            for (int x = 0; x < gameArea[y].length; x++) {
                //Action sur gameArea[y][x]
                if (!gameArea[y][x].isBorder) {
                    int countVirgin = 0;
                    int countBomb = 0;
                    List<Cell> virginCells = new ArrayList();
                    Cell cellNW = gameArea[y - 1][x - 1];
                    Cell cellN = gameArea[y - 1][x];
                    Cell cellNE = gameArea[y - 1][x + 1];
                    Cell cellW = gameArea[y][x - 1];
                    Cell cellE = gameArea[y][x + 1];
                    Cell cellSW = gameArea[y + 1][x - 1];
                    Cell cellS = gameArea[y + 1][x];
                    Cell cellSE = gameArea[y + 1][x + 1];

                    if (cellNW.isVirgin) {
                        virginCells.add(cellNW);
                        countVirgin += 1;
                    } else if (cellNW.isBomb) {
                        countBomb += 1;
                    }
                    if (cellN.isVirgin) {
                        virginCells.add(cellN);
                        countVirgin += 1;
                    } else if (cellN.isBomb) {
                        countBomb += 1;
                    }
                    if (cellNE.isVirgin) {
                        virginCells.add(cellNE);
                        countVirgin += 1;
                    } else if (cellNE.isBomb) {
                        countBomb += 1;
                    }
                    if (cellW.isVirgin) {
                        virginCells.add(cellW);
                        countVirgin += 1;
                    } else if (cellW.isBomb) {
                        countBomb += 1;
                    }
                    if (cellE.isVirgin) {
                        virginCells.add(cellE);
                        countVirgin += 1;
                    } else if (cellE.isBomb) {
                        countBomb += 1;
                    }
                    if (cellSW.isVirgin) {
                        virginCells.add(cellSW);
                        countVirgin += 1;
                    } else if (cellSW.isBomb) {
                        countBomb += 1;
                    }
                    if (cellS.isVirgin) {
                        virginCells.add(cellS);
                        countVirgin += 1;
                    } else if (cellS.isBomb) {
                        countBomb += 1;
                    }
                    if (cellSE.isVirgin) {
                        virginCells.add(cellSE);
                        countVirgin += 1;
                    } else if (cellSE.isBomb) {
                        countBomb += 1;
                    }

                    if (gameArea[y][x].getNbr() != 0 && gameArea[y][x].getNbr() == (countVirgin + countBomb)) {
                        for (int i = 0; i < virginCells.size(); i++) {
                            if (!virginCells.get(i).isBomb) {
                                clickCell(virginCells.get(i), InputEvent.BUTTON3_DOWN_MASK);
                                virginCells.get(i).isBomb = true;
                                virginCells.get(i).isVirgin = false;
                                blocked = false;
                            }
                        }
                    }
                }
            }
        }
        return blocked;
    }

    public boolean discoverNewCells() {
        boolean blocked = true;
        for (int y = 0; y < gameArea.length; y++) {
            for (int x = 0; x < gameArea[y].length; x++) {
                //Action sur gameArea[y][x]
                if (!gameArea[y][x].isBorder) {
                    int countBomb = 0;
                    List<Cell> virginCells = new ArrayList();
                    Cell cellNW = gameArea[y - 1][x - 1];
                    Cell cellN = gameArea[y - 1][x];
                    Cell cellNE = gameArea[y - 1][x + 1];
                    Cell cellW = gameArea[y][x - 1];
                    Cell cellE = gameArea[y][x + 1];
                    Cell cellSW = gameArea[y + 1][x - 1];
                    Cell cellS = gameArea[y + 1][x];
                    Cell cellSE = gameArea[y + 1][x + 1];

                    if (cellNW.isVirgin) {
                        virginCells.add(cellNW);
                    } else if (cellNW.isBomb) {
                        countBomb += 1;
                    }
                    if (cellN.isVirgin) {
                        virginCells.add(cellN);
                    } else if (cellN.isBomb) {
                        countBomb += 1;
                    }
                    if (cellNE.isVirgin) {
                        virginCells.add(cellNE);
                    } else if (cellNE.isBomb) {
                        countBomb += 1;
                    }
                    if (cellW.isVirgin) {
                        virginCells.add(cellW);
                    } else if (cellW.isBomb) {
                        countBomb += 1;
                    }
                    if (cellE.isVirgin) {
                        virginCells.add(cellE);
                    } else if (cellE.isBomb) {
                        countBomb += 1;
                    }
                    if (cellSW.isVirgin) {
                        virginCells.add(cellSW);
                    } else if (cellSW.isBomb) {
                        countBomb += 1;
                    }
                    if (cellS.isVirgin) {
                        virginCells.add(cellS);
                    } else if (cellS.isBomb) {
                        countBomb += 1;
                    }
                    if (cellSE.isVirgin) {
                        virginCells.add(cellSE);
                    } else if (cellSE.isBomb) {
                        countBomb += 1;
                    }

                    if (gameArea[y][x].getNbr() != 0 && gameArea[y][x].getNbr() == countBomb) {
                        for (int i = 0; i < virginCells.size(); i++) {
                            clickCell(virginCells.get(i), InputEvent.BUTTON1_DOWN_MASK);
                            virginCells.get(i).isVirgin = false;
                            blocked = false;
                        }
                    }
                }
            }
        }
        return blocked;
    }

    public Point searchExactly(BufferedImage sourceOfSearch, int[][] cible, String nomCible, Point beginZoneOfSearch, Point endZoneOfSearch) {
        //System.out.println("Scanning en cours, recherche de : " + nomCible);

        // Parcours les colonnes de la zone
        for (int y = beginZoneOfSearch.y; y < endZoneOfSearch.y; y++) {
            // Parcours les lignes de la zone
            for (int x = beginZoneOfSearch.x; x < endZoneOfSearch.x; x++) {

                // Matches sur true à la base, pour faire au moins une fois la boucle
                boolean matches = true;
                // (SI MATCH) vérifie les autres pixels d'après le modèle :
                // Parcours les colonnes du modèle
                for (int yy = 0; yy < cible.length && matches; yy++) {
                    // Parcours les lignes du modèle
                    for (int xx = 0; xx < cible[yy].length && matches; xx++) {

                        // Vérifie le matching
                        if (cible[yy][xx] != sourceOfSearch.getRGB(x + xx, y + yy)) {
                            matches = false;
                        }
                    }
                }

                if (matches) {
                    int realX = x + margeLeft;
                    int realY = y + margeTop;

                    if (nomCible != "Cellule vierge") {
                        System.out.println(nomCible + " trouvé en position " + realX + ", " + realY);
                    }
                    return new Point(realX, realY);
                }
            }
        }
        //System.out.println(nomCible + " non trouvé");
        return new Point(0, 0);
    }

    public void clickCell(Cell cellToClick, int click) {
        try {
            Robot robot = new Robot();

            //System.out.println("Clique sur la cellule : " + cellToClick.getxPos() + "," + cellToClick.getyPos());
            robot.mouseMove((int) cellToClick.getCoinTopLeft().x + (cellToClick.getWidth() / 2), cellToClick.getCoinTopLeft().y + (cellToClick.getHeight() / 2));

            robot.mousePress(click);
            this.sleep(100);
            robot.mouseRelease(click);

        } catch (AWTException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void afficheTableau() {
        for (int y = 0; y < gameArea.length; y++) {
            for (int x = 0; x < gameArea[y].length; x++) {
                //Action sur gameArea[y][x]

                if (gameArea[y][x].isBorder) {
                    System.out.print(" + ");
                } else if (gameArea[y][x].isBomb) {
                    System.out.print(" * ");
                } else if (gameArea[y][x].isVirgin) {
                    System.out.print(" - ");
                } else {
                    System.out.print(" " + gameArea[y][x].getNbr() + " ");
                }

                //System.out.print(gameArea[y][x].getCoinTopLeft().x + ";");
                //System.out.print(gameArea[y][x].getxPos() + "," + gameArea[y][x].getyPos() + " ; ");
            }
            System.out.println("");
        }
    }

    public void analyseRGBImage(BufferedImage target) {
        for (int y = 0; y < target.getHeight(); y++) {
            System.out.print("{");
            for (int x = 0; x < target.getWidth(); x++) {
                //Action sur gameArea[y][x]
                System.out.print(target.getRGB(x, y));
                System.out.print(", ");
            }
            System.out.print("},");
            System.out.println("");
        }
    }
}
