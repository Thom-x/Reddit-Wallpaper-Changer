package fr.maugin.thomas.view;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fr.maugin.thomas.injection.AppModule;
import fr.maugin.thomas.view.controller.AppController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.BuilderFactory;
import rx.observables.JavaFxObservable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

import static java.awt.SystemTray.getSystemTray;
import static java.awt.Toolkit.getDefaultToolkit;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * User: thoma
 * Date: 30/03/2016
 * Time: 18:54
 */
public class GUIApp extends Application {

    private static final String APP_TITLE = "Reddit Wallpaper Changer";
    private Stage stage;
    private boolean firstTime = true;

    private AppController controller = Guice.createInjector(new AppModule()).getInstance(AppController.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        stage.setOnCloseRequest(e -> Platform.runLater(() -> {
            Platform.exit();
        }));

        JavaFxObservable.fromObservableValue(stage.iconifiedProperty())
                .filter(v -> v)
                .subscribe(iconified -> {
                    stage.hide();
                    invokeLater(() -> {
                        final TrayIcon trayIcon = getTray();
                        SystemTray tray = getSystemTray();
                        try {
                            tray.add(trayIcon);
                            if (firstTime) {
                                trayIcon.displayMessage(APP_TITLE,
                                        "The application has been minimized",
                                        TrayIcon.MessageType.INFO);
                                firstTime = false;
                            }
                        } catch (AWTException e2) {
                            e2.printStackTrace();
                            Platform.exit();
                        }
                    });
                });


        Platform.setImplicitExit(false);

        BuilderFactory builderFactory = new JavaFXBuilderFactory();
        FXMLLoader mainViewLoader = new FXMLLoader(getClass().getResource("/fxml/app.fxml"), null, builderFactory, clazz ->  controller);
        Parent root = mainViewLoader.load();
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getClassLoader().getResourceAsStream("icon/icon-big.png")));
        stage.setTitle(APP_TITLE);
        stage.setScene(new Scene(root, 600, 400));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private TrayIcon getTray() {
        try {
            // ensure awt toolkit is initialized.
            getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!SystemTray.isSupported()) {
                Platform.exit();
            }

            URL imageLoc = getClass().getClassLoader().getResource("icon/icon.png");
            Image image = ImageIO.read(imageLoc);
            TrayIcon trayIcon = new TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(() -> showStage(trayIcon)));

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            MenuItem openItem = new MenuItem("Show");
            openItem.addActionListener(event -> Platform.runLater(() -> showStage(trayIcon)));

            MenuItem nextItem = new MenuItem("Next");
            nextItem.addActionListener(event -> Platform.runLater(() -> {
                controller.next();
            }));

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(event -> {
                Platform.exit();
                getSystemTray().remove(trayIcon);
            });

            // setup the popup menu for the application.
            final PopupMenu popup = new PopupMenu();
            popup.add(nextItem);
            popup.addSeparator();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            return trayIcon;
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
        return null;
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     *
     * @param trayIcon
     */
    private void showStage(TrayIcon trayIcon) {
        if (stage != null) {
            getSystemTray().remove(trayIcon);
            stage.setIconified(false);
            stage.show();
            stage.toFront();
        }
    }
}
