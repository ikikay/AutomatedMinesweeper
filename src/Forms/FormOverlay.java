/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Forms;

import Classes.Solver;
import static AutomatedMinesweeper.AutomatedMinesweeper.*;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 *
 * @author Ikikay
 */
public class FormOverlay extends javax.swing.JFrame {

    Solver theSolver;
    Point pointPosition = null;
    boolean isTransparency = false;

    public FormOverlay() {

        setTitle("Overlay");							// Applique le titre à la fenêtre
        setAlwaysOnTop(true);                                                   // Oblige la fenêtre à être au premier plan
        setResizable(false);
        setBounds(
                margeLeft,
                margeTop,
                Toolkit.getDefaultToolkit().getScreenSize().width - margeLeft - margeRight,
                Toolkit.getDefaultToolkit().getScreenSize().height - margeTop - margeBot);  // Fait une fenêtre a l'emplacement x, y de largeur x hauteur 
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);				// Ferme l'application si il n'y à plus de fenêtre
        setUndecorated(true);							// Supprime la barre et les contours de la fenêtre
        //setLocationRelativeTo(null);						// Centre la fenêtre
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                if (pointPosition != null) {
                    g.setColor(Color.red);
                    //g.drawOval(pointPosition.x - 7, pointPosition.y - 7, 14, 15); // Crée un rond à l'endroit du clique
                }
            }
        };                                                                      // Création d'un JPanel

        setLayout(new GridBagLayout());						// Création d'un Layaout de type GridBag
        GridBagConstraints gbC = new GridBagConstraints();			// Le gbC va définir la position et la taille des éléments
        gbC.anchor = GridBagConstraints.NORTHWEST;				// Met les items en haut à gauche
        gbC.fill = GridBagConstraints.BOTH;					// Prend toute la place diponible en hauteur et en largeur
        gbC.insets = new Insets(5, 5, 5, 5);					// insets défini la marge entre les composant new Insets(margeSupérieure, margeGauche, margeInférieur, margeDroite) */

        gbC.gridx = 0;
        gbC.gridy = 0;
        JButton bHide = new JButton("Hide");					// Créer un bouton "Hide"
        bHide.setSize(100, 50);							// de taille 100x50
        panel.add(bHide, gbC);							// ajoute ce bouton, au panel
        bHide.addActionListener((event) -> {					// Créer une " "micro fonction" " lorsque quelque chose se passe sur le bouton
            //Actions lors des cliques sur le bouton 
            setBackground(new Color(255, 255, 255, 1));				// Rend la fenêtre transparente
            isTransparency = false;
        });
        gbC.gridx = 1;
        gbC.gridy = 0;
        JButton bShow = new JButton("Show");					// Créer un bouton "Show"
        bShow.setSize(100, 50);							// de taille 100x50
        panel.add(bShow, gbC);							// ajoute ce bouton, au panel
        bShow.addActionListener((event) -> {					// Créer une " "micro fonction" " lorsque quelque chose se passe sur le bouton
            //Actions lors des cliques sur le bouton 
            setBackground(new Color(255, 255, 255, 128));				// Enlève la transparence de la fenêtre
            isTransparency = false;
        });
        gbC.gridx = 2;
        gbC.gridy = 0;
        JButton bAnnuler = new JButton("Annuler");				// Créer un bouton "Annuler"
        bAnnuler.setSize(100, 50);						// de taille 100x50
        panel.add(bAnnuler, gbC);						// ajoute ce bouton, au panel
        bAnnuler.addActionListener((event) -> {					// Créer une " "micro fonction" " lorsque quelque chose se passe sur le bouton
            //Actions lors des cliques sur le bouton 
            System.exit(0);
        });
        gbC.gridx = 3;
        gbC.gridy = 0;
        JButton bValider = new JButton("Valider");				// Créer un bouton "Valider"
        bValider.setSize(100, 50);						// de taille 100x50
        panel.add(bValider, gbC);						// ajoute ce bouton, au panel
        bValider.addActionListener((event) -> {					// Créer une " "micro fonction" " lorsque quelque chose se passe sur le bouton
            //Actions lors des cliques sur le bouton 
            
            if (theSolver != null) {
                theSolver.close();
            }
            theSolver = new Solver(this.getX(), this.getY(), this.getWidth(), this.getHeight());

            theSolver.start();
            
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                //TODO : Double clique à régler
                System.out.println("Clique détecté");
                pointPosition = arg0.getPoint();                                // Enregistre la position du clique
                repaint();                                                      // Repaint avec un rond à l'emplacement de pointPosition (position du clique)
                setBackground(new Color(255, 255, 255, 0));                           // Rend la fenêtre transparente
                isTransparency = true;

                try {
                    Robot robot = new Robot();
                    if (arg0.getButton() == 1) {
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);         // Clique
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);       // Relache le clique

                    } else if (arg0.getButton() == 3) {
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);         // Clique
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);       // Relache le clique
                    }
                } catch (AWTException ex) {
                    Logger.getLogger(FormOverlay.class.getName()).log(Level.SEVERE, null, ex);
                }

                setBackground(new Color(255, 255, 255, 1));                           // Rend la fenêtre non transparente
                isTransparency = false;
            }
        });

        setContentPane(panel);							// Ajoute le JPanel (panel) au JFrame
        setBackground(new Color(255, 255, 255, 1));                                  // Rend la fenêtre transparente
        isTransparency = false;
        setVisible(true);							// Rend la fenêtre visible
    }
}
