/*
    SongScribe song notation program
    Copyright (C) 2006 Csaba Kavai

    This file is part of SongScribe.

    SongScribe is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    SongScribe is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Created on Sep 10, 2007
*/
package songscribe.data;

import org.apache.log4j.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

/**
 * @author Csaba Kávai
 */
public class MyDesktop {
    private static Logger logger = Logger.getLogger(MyDesktop.class);

    private static boolean supported;
    private static Class clazz;
    private Object desktop;

    private MyDesktop(Object desktop) {
        this.desktop = desktop;
    }

    public static boolean isDesktopSupported() {
        if (clazz == null) {
            try {
                clazz = Class.forName("java.awt.Desktop");
                supported = (Boolean) clazz.getMethod("isDesktopSupported").invoke(null);
            }
            catch (ClassNotFoundException e) {
                clazz = MyDesktop.class;
                supported = false;
            }
            catch (Exception e) {
                clazz = MyDesktop.class;
                supported = false;
                logger.error("mydesktop issupported static", e);
            }
        }
        return supported;
    }

    public static MyDesktop getDesktop() {
        if (isDesktopSupported()) {
            try {
                return new MyDesktop(clazz.getMethod("getDesktop").invoke(null));
            }
            catch (Exception e) {
                logger.error("mydesktop getdesktop", e);
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void browse(URI uri) {
        if (isSupported(Action.BROWSE)) {
            try {
                clazz.getMethod("browse", URI.class).invoke(desktop, uri);
            }
            catch (Exception e) {
                logger.error("mydesktop browse", e);
            }
        }
    }

    public void mail(URI uri) {
        if (isSupported(Action.MAIL)) {
            try {
                clazz.getMethod("mail", URI.class).invoke(desktop, uri);
            }
            catch (Exception e) {
                logger.error("mydesktop mail", e);
            }
        }
    }

    public void open(File file) throws Exception {
        if (isSupported(Action.OPEN)) {
            try {
                clazz.getMethod("open", File.class).invoke(desktop, file);
            }
            catch (NoSuchMethodException e) {
                logger.error("mydesktop open", e);
            }
            catch (InvocationTargetException e) {
                throw new Exception(e.getTargetException());
            }
            catch (IllegalAccessException e) {
                logger.error("mydesktop open", e);
            }
        }
    }

    private boolean isSupported(Action p) {
        if (isDesktopSupported()) {
            try {
                Class actionClass = Class.forName("java.awt.Desktop$Action");
                Object action = actionClass.getMethod("valueOf", String.class).invoke(null, p.name());
                return (Boolean) clazz.getMethod("isSupported", actionClass).invoke(desktop, action);
            }
            catch (Exception e) {
                logger.error("mydesktop issupported object", e);
                return false;
            }
        }
        else {
            return false;
        }
    }

    public enum Action { BROWSE, MAIL, OPEN }
}
