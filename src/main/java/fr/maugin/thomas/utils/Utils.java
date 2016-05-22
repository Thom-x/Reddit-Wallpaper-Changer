package fr.maugin.thomas.utils;

import fr.maugin.thomas.App;
import fr.maugin.thomas.domain.pojo.CustomFormatter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public class Utils {

    private static final String LOG_FILE = "/app.log";

    /**
     * The byte[] returned by MessageDigest does not have a nice
     * textual representation, so some form of encoding is usually performed.
     * <p>
     * This implementation follows the example of David Flanagan's book
     * "Java In A Nutshell", and converts a byte array into a String
     * of hex characters.
     * <p>
     * Another popular alternative is to use a "Base64" encoding.
     */
    public static String hexEncode(byte[] aInput) {
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (byte b : aInput) {
            result.append(digits[(b & 0xf0) >> 4]);
            result.append(digits[b & 0x0f]);
        }
        return result.toString();
    }

    /**
     * @return
     * @throws URISyntaxException
     */
    public static File getAppPath(Class clazz) throws URISyntaxException {
        File appPath = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        return appPath.isFile() ? appPath.getParentFile() : appPath;
    }

    /**
     * @param clazz
     * @return
     */
    public static Logger getLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            File logPath = new File(Utils.getAppPath(App.class).getAbsolutePath() + LOG_FILE);
            if (!logPath.exists()) {
                logPath.createNewFile();
            }
            fh = new FileHandler(logPath.getAbsolutePath(), true);
            logger.addHandler(fh);
            CustomFormatter formatter = new CustomFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException | URISyntaxException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return logger;
    }
}
