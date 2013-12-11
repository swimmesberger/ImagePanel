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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.TransferHandler;

public class ImageTransferhandler extends TransferHandler
{
    private static DataFlavor urlFlavor;
    private static DataFlavor uriList;
    
    public static DataFlavor[] supportedFlavors = null;
    
    static
    {
        try
        {
            urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
            uriList = new DataFlavor("text/uri-list; class=java.lang.String; charset=Unicode");
            supportedFlavors = new DataFlavor[]
            {
                urlFlavor, 
                DataFlavor.imageFlavor, 
                DataFlavor.javaFileListFlavor, 
                uriList,
                DataFlavor.stringFlavor
            };
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(ImageTransferhandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private IHasImg img;

    public ImageTransferhandler(IHasImg img)
    {
        this.img = img;
    }
   
    @Override
    public boolean canImport(TransferSupport support)
    {
        DataFlavor[] dataFlavors = support.getDataFlavors();
        for (DataFlavor f : supportedFlavors)
        {
            String suppMime = f.getMimeType();
            for(DataFlavor dataFlavor : dataFlavors){
                if(dataFlavor.getMimeType().equals(suppMime)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        Transferable transferable = support.getTransferable();
        DataFlavor[] dataFlavors = support.getDataFlavors();
        BufferedImage transferData = null;
        //check if we support one of the data flavors
        for(DataFlavor dataFlavor : dataFlavors){
            String mimeType = dataFlavor.getMimeType();
            if(mimeType.equals(urlFlavor.getMimeType())){
                transferData = getFromURL(transferable);
            } else if(mimeType.equals(DataFlavor.imageFlavor.getMimeType())){
                transferData = getImage(transferable);
            }else if(mimeType.equals(DataFlavor.javaFileListFlavor.getMimeType())){
                transferData = getImageFromFile(transferable);
            }else if(mimeType.equals(uriList.getMimeType())){
                transferData = getFromFileString(transferable);
            }else if(mimeType.equals(DataFlavor.stringFlavor.getMimeType())){
                transferData = getFromPlainString(transferable);
            }
            if(transferData != null){
                img.setImage(transferData);
                break;
            }
        }
        if(transferData == null){
            Logger.getLogger(ImageTransferhandler.class.getName()).log(Level.INFO, "Unsupported flavor: {0}", dataFlavors[0].getMimeType());
            return false;
        }else{
            return true;
        }
    }

    private BufferedImage getImageFromFile(Transferable transferable)
    {
        try
        {
            Collection col = (Collection) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            ArrayList arrList = new ArrayList(col);
            return getImageFromList(arrList);
        } catch (UnsupportedFlavorException | IOException ex)
        {
            //Logger.getLogger(ImageTransferhandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private BufferedImage getImageFromList(List l) throws IOException
    {
        File f = (File) l.get(0);
        return ImageIO.read(f);
    }

    private BufferedImage getImage(Transferable transferable)
    {
        try
        {
            return (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
        } catch (UnsupportedFlavorException | IOException ex)
        {
            //Logger.getLogger(ImageTransferhandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private BufferedImage getFromURL(Transferable transferable)
    {
        try
        {
            java.net.URL url = (java.net.URL) transferable.getTransferData(urlFlavor);
            return ImageIO.read(url.openStream());
        } catch (UnsupportedFlavorException | IOException ex)
        {
            //Logger.getLogger(ImageTransferhandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static java.util.List textURIListToFileList(String data)
    {
        java.util.List list = new java.util.ArrayList(1);
        for (java.util.StringTokenizer st = new java.util.StringTokenizer(data, "\r\n");
        st.hasMoreTokens();)
        {
            String s = st.nextToken();
            if (s.startsWith("#"))
            {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try
            {
                java.net.URI uri = new java.net.URI(s);
                java.io.File file = new java.io.File(uri);
                list.add(file);
            } catch (java.net.URISyntaxException e)
            {
                // malformed URI
            } catch (IllegalArgumentException e)
            {
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }

    private BufferedImage getFromFileString(Transferable transferable)
    {
        try
        {
            String str = (String) transferable.getTransferData(uriList);
            List textURIListToFileList = textURIListToFileList(str);
            return getImageFromList(textURIListToFileList);
        } catch (UnsupportedFlavorException | IndexOutOfBoundsException | IOException ex)
        {
            //ignore not supported
        }
        return null;
    }
    
    private BufferedImage getFromPlainString(Transferable transferable){
        try {
            String str = (String) transferable.getTransferData(DataFlavor.stringFlavor);
            //check if string is a url
            try{
                URL url = new URL(str);
                //download image from url
                return ImageIO.read(url);
            }catch(MalformedURLException ex){
                //string is no url - try if its a file
                File f = new File(str);
                if(f.exists()){
                    return ImageIO.read(f);
                }
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            //ignore not supported
        }
        return null;
    }
}
