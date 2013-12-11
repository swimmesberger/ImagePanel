/*
 * Copyright (C) 2013 Thedeath<www.fseek.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.fseek.simon.gui.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class ImagePanel extends JPanel implements IHasImg{
    private static final Border DEFAULT_FOCUS_BORDER = new LineBorder(Color.BLACK);
    
    private Border focusBorder = DEFAULT_FOCUS_BORDER;
    
    private BufferedImage image;
    private final JLabel infoLabel;
    
    public ImagePanel(){
        this.infoLabel = new JLabel("Drop image here...", JLabel.HORIZONTAL);
        this.setTransferHandler(new ImageTransferhandler(this));
        this.setLayout(new BorderLayout());
        this.add(infoLabel, BorderLayout.CENTER);
        this.setFocusable(true);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ImagePanel.this.requestFocusInWindow();
            }
        });
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ImagePanel.this.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                ImagePanel.this.repaint();
            }
        });
        setMappings();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        super.paint(g2d);
        if(this.image != null){
            g2d.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        }
        if(this.hasFocus()){
            this.setBorder(focusBorder);
        }else{
            this.setBorder(null);
        }
    }

    @Override
    public void setImage(BufferedImage img) {
        if(this.image == img)return;
        this.image = img;
        this.infoLabel.setVisible(this.image == null);
        this.repaint();
    }

    @Override
    public BufferedImage getImage() {
        return this.image;
    }

    public Border getFocusBorder() {
        return focusBorder;
    }

    public void setFocusBorder(Border focusBorder) {
        this.focusBorder = focusBorder;
    }
    
    /**
     * Paste with (ctrl v)
     * @see http://docs.oracle.com/javase/tutorial/uiswing/dnd/listpaste.html
     */
    private void setMappings()
    {
        int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
        
        InputMap imap = this.getInputMap();
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, keyMask), 
                TransferHandler.getPasteAction().getValue(Action.NAME));
    }
}
