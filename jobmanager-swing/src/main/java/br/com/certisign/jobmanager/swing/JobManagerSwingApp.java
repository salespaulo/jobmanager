/**
 * jobmanager-swing
 * 
 * Criada em 16/03/2011 13:02:52
 * 
 * Direito de cópia reservado é Certisign Certificadora Digital S.A.
 * Todos os direitos são reservador em propriedade dda empresa
 * ------------- Certisign Certificadora Digital S.A. --------------
 * O uso deste produto é sujeito aos termos de licen�a
 */
package br.com.certisign.jobmanager.swing;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.certisign.jobmanager.core.Loader;
import br.com.certisign.jobmanager.core.SchedulerManager;
import br.com.certisign.jobmanager.core.SchedulerManagerFactory;

/**
 * TODO (psales 16/03/2011) - javadoc
 * 
 * @author psales
 * @since 16/03/2011
 */
public class JobManagerSwingApp {

	/** TODO (psales 16/03/2011) - javadoc */
	private static final Logger logger = LoggerFactory.getLogger(JobManagerSwingApp.class);

	/**
	 * TODO (psales 16/03/2011) - javadoc
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JobManagerSwingApp.createAndShowGUI();
			}
		});
	}
	
	/** TODO (psales 16/03/2011) - javadoc */
	private static void createAndShowGUI() {
		System.out.println("starting...");
		if (!SystemTray.isSupported()) {
			fail();
		}
		
		final PopupMenu popup = new PopupMenu();

		final Image icon = createImage("icon_2.gif", "Certisign Job Manager Icon");
		final Image activeIcon = createImage("icon.gif", "Certisign Job Manager Active Icon");
		final TrayIcon trayIcon = new TrayIcon(icon, "Certisign Job Manager System", popup);
		final SystemTray systemTray = SystemTray.getSystemTray();

		final MenuItem startItem = new MenuItem("Iniciar", new MenuShortcut('i'));
		final MenuItem stopItem = new MenuItem("Parar", new MenuShortcut('p'));
		final MenuItem refreshItem = new MenuItem("Recarregar", new MenuShortcut('r'));
		final MenuItem exitItem = new MenuItem("Sair", new MenuShortcut('s'));
		
		popup.add(startItem);
		popup.add(stopItem);
//		popup.add(refreshItem);
		popup.addSeparator();
		popup.add(exitItem);

		final SchedulerManager schedulerManager = SchedulerManagerFactory.getXmlSchedulerManager();
		
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: (psales 16/03/2011) Implementar as telas do sistema e chamar aqui!
            }
        });

        startItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				schedulerManager.doSchedulers();
				startItem.setEnabled(false);
				stopItem.setEnabled(true);
				trayIcon.setImage(activeIcon);
			}
		});
        
        stopItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				schedulerManager.finalizeSchedulers();
				startItem.setEnabled(true);
				stopItem.setEnabled(false);
				trayIcon.setImage(icon);
			}
		});
        
        refreshItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				schedulerManager.refreshSchedulers();
			}
		});
        
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				schedulerManager.finalizeSchedulers();
				systemTray.remove(trayIcon);
				System.exit(0);
			}
		});
		
		try {
			stopItem.setEnabled(false);
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			fail();
		}
		
	}

	/** TODO (psales 16/03/2011) - javadoc */
	protected static Image createImage(String path, String description) {
        final URL imageURL = Loader.getResource(path);
        
        if (imageURL == null) {
            logger.error("Imagem n�o encontrada: " + path);
            return null;
        }

        return (new ImageIcon(imageURL, description)).getImage();
    }
	
	/** TODO (psales 16/03/2011) - javadoc */
	private static void fail() {
		logger.error("System Tray n�o � suportado pelo sistema operacional!");
		System.exit(1);
	}
	
}
