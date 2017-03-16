package fr.maugin.thomas.service.pojo;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;
import fr.maugin.thomas.service.api.IWallpaperChanger;
import rx.Observable;

import java.io.File;
import java.util.HashMap;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public class WallpaperChanger implements IWallpaperChanger {

    public void changeWallpaper(File file) {
        Observable.just(file)
                .filter(File::exists)
                .filter(File::isFile)
                .map(File::getAbsolutePath)
                .subscribe(path -> {
                    SPI.INSTANCE.SystemParametersInfo(
                            new WinDef.UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
                            new WinDef.UINT_PTR(0),
                            path,
                            new WinDef.UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
                });
    }

    public static void main(String[] args) {
        //supply your own path instead of using this one
        String path = "G:\\Dropbox\\Images\\Wallpaper\\thom-x.jpg";

        SPI.INSTANCE.SystemParametersInfo(
                new WinDef.UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
                new WinDef.UINT_PTR(0),
                path,
                new WinDef.UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
    }

    private interface SPI extends StdCallLibrary {

        //from MSDN article
        long SPI_SETDESKWALLPAPER = 20;
        long SPIF_UPDATEINIFILE = 0x01;
        long SPIF_SENDWININICHANGE = 0x02;



        SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class, new HashMap<String, Object>() {
            {
                put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            }
        });

        boolean SystemParametersInfo(
                WinDef.UINT_PTR uiAction,
                WinDef.UINT_PTR uiParam,
                String pvParam,
                WinDef.UINT_PTR fWinIni
        );
    }
}
