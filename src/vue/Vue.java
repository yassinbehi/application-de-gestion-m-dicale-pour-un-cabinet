/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vue;


import java.awt.event.ActionListener;
import javax.swing.*;

public interface Vue {
    JPanel getPanel();
    void mettreAJour();
    void ajouterListener(ActionListener listener);
}