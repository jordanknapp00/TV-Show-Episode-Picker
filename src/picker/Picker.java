package picker;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Picker {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				@SuppressWarnings("unused")
				Picker p = new Picker();
			}
		});
	}
	
	private JFrame frame;
	private JPanel topPanel;
	private JPanel bottomPanel;
	
	private JTextArea results;
	
	private JButton generateButton;
	private JButton loadButton;
	private JButton bufferButton;
	
	private boolean fileLoaded;
	private ArrayList<String> episodes;
	private ArrayList<String> cannotGet;
	private File file;
	private boolean openedBufferPanel;
	
	public Picker() {
		frame = new JFrame("TV Show Episode Picker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(760, 175);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		results = new JTextArea();
		results.setEditable(false);
		results.setFont(results.getFont().deriveFont(18f));
		
		//Set look and feel
		try {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			int hour = ZonedDateTime.now().getHour();
			int min = ZonedDateTime.now().getMinute();
			int sec = ZonedDateTime.now().getSecond();
			System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
		} catch (InstantiationException e) {
			int hour = ZonedDateTime.now().getHour();
			int min = ZonedDateTime.now().getMinute();
			int sec = ZonedDateTime.now().getSecond();
			System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
		} catch (IllegalAccessException e) {
			int hour = ZonedDateTime.now().getHour();
			int min = ZonedDateTime.now().getMinute();
			int sec = ZonedDateTime.now().getSecond();
			System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
		} catch (UnsupportedLookAndFeelException e) {
			int hour = ZonedDateTime.now().getHour();
			int min = ZonedDateTime.now().getMinute();
			int sec = ZonedDateTime.now().getSecond();
			System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
		}
		
		fileLoaded = false;
		episodes = new ArrayList<String>();
		cannotGet = new ArrayList<String>();
		openedBufferPanel = false;
		
		topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createTitledBorder("Results"));
		topPanel.setLayout(new BorderLayout());
		topPanel.add(results);
		
		//attempt to set up the load button correctly
		loadButton = new JButton("Load");
		try {
			Image loadImage = ImageIO.read(getClass().getResource("/images/Open.png"));
			loadButton.setIcon(new ImageIcon(loadImage));
		} catch (IOException e) {
			int hour = ZonedDateTime.now().getHour();
			int min = ZonedDateTime.now().getMinute();
			int sec = ZonedDateTime.now().getSecond();
			System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
		}
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridBagLayout());
		generateButton = new JButton("Generate");
		bufferButton = new JButton("Recently-seen episodes");
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weighty = 1;
		gc.weightx = .2;
		gc.anchor = GridBagConstraints.BASELINE;
		gc.fill = GridBagConstraints.HORIZONTAL;
		bottomPanel.add(loadButton, gc);
		gc.gridx = 1;
		gc.weightx = .5;
		bottomPanel.add(generateButton, gc);
		gc.gridx = 2;
		gc.weightx = .3;
		bottomPanel.add(bufferButton, gc);
		
		loadButton.addActionListener(new LoadButtonActionListener());
		generateButton.addActionListener(new GenerateButtonActionListener());
		bufferButton.addActionListener(new BufferButtonActionListener());
		
		frame.getContentPane().setLayout(new GridBagLayout());
		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.weighty = .99;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.BOTH;
		frame.add(topPanel, gc);
		gc.weighty = .01;
		gc.gridy = 1;
		frame.add(bottomPanel, gc);
		
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/jery.png")));
		
		DropTarget fileDropTarget = new DropTarget() {
			private static final long serialVersionUID = 1L;

			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					if(evt.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						evt.acceptDrop(DnDConstants.ACTION_COPY);
						@SuppressWarnings("unchecked")
						List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						LoadButtonActionListener lbal = new LoadButtonActionListener();
						for(File at: droppedFiles) {
							file = at;
							lbal.readFile();
						}
					}
					else {
						evt.rejectDrop();
					}
				} catch(Exception ex) {
					results.append("Exception in drag/drop file system!\n");
				}
			}
		};
		
		results.setDropTarget(fileDropTarget);
		
		frame.setVisible(true);
	}
	
	private class LoadButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(fileLoaded) {
				if(JOptionPane.showConfirmDialog(frame, "You already have a file "
						+ "loaded.\nLoad another one?", "Smash Character Picker",
						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
				else {
					episodes.clear();
				}
			}
			
			JFileChooser fileChooser = new JFileChooser(".");
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt files", "txt");
			fileChooser.setFileFilter(filter);
			int r = fileChooser.showOpenDialog(frame);
			file = fileChooser.getSelectedFile();
			
			if (r == JFileChooser.APPROVE_OPTION) {
				readFile();
			}
		}
		
		private void readFile() {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				
				String lineAt = in.readLine();
				while(lineAt != null && !lineAt.equals("Cannot get:")) {
					episodes.add(lineAt);
					lineAt = in.readLine();
				}
				if(lineAt == null) {
					in.close();
					throw new IOException("No cannot get! Not a valid file");
				}
				lineAt = in.readLine();
				while(lineAt != null) {
					cannotGet.add(lineAt);
					lineAt = in.readLine();
				}
				in.close();
			} catch (FileNotFoundException e) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
				results.append("File " + file.getName() + " not found!\n");
				fileLoaded = false;
				return;
			} catch(IOException ioe) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + ioe);
				results.append("IOException in reading " + file.getName() + ".\n"
						+ "This means it is not a valid file.\n" +
						"Please load a valid file.\n");
				fileLoaded = false;
				return;
			}
			
			System.out.println("[DEBUG]: The following data has been loaded as the current file:");
			System.out.println("         EPISODES:");
			for(int at = 0; at < episodes.size(); at++) {
				System.out.println("              " + episodes.get(at));
			}
			System.out.println("         CANNOT GET:");
			for(int at = 0; at < cannotGet.size(); at++) {
				System.out.println("     " + cannotGet.get(at));
			}
			
			fileLoaded = true;
			results.append("Loaded file: ");
			results.append(file.getName() + "\n");
		}
	}
	
	private class GenerateButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(!fileLoaded) {
				JOptionPane.showMessageDialog(frame, "You must load a file first!",
						"TV Show Episode Picker", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			results.setText("");
			String got = "";
			while(got.equals("") || cannotGet.contains(got)) {
				got = episodes.get(ThreadLocalRandom.current().nextInt(0, episodes.size()));
			}
			
			cannotGet.add(got);
			try {
				FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				out.println(got);
				out.close();
			} catch (IOException e1) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e1);
				results.append("IOException in writing to " + file.getName() + ".\n");
				fileLoaded = false;
				return;
			}
			
			results.append(got);
			results.append("\n\n");
			results.append("You have recently seen " + cannotGet.size() + "/" + episodes.size() + " episodes.");
		}
	}
	
	private class BufferButtonActionListener implements ActionListener {
		
		private JFrame bufferFrame;
		private JPanel bufferPanel;
		private JPanel bottomPanel;
		
		private JButton clearButton;
		
		private ArrayList<JButton> buttons;
		
		public void actionPerformed(ActionEvent e) {
			if(openedBufferPanel) {
				return;
			}
			else if(!fileLoaded) {
				JOptionPane.showMessageDialog(frame, "You must load a file first!",
						"TV Show Episode Picker", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			bufferFrame = new JFrame("Buffer");
			bufferFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			bufferFrame.setSize(250, 435);
			bufferFrame.setResizable(false);
			
			int xPos = frame.getX() - bufferFrame.getWidth();
			
			if(xPos < 0) {
				xPos = frame.getX() + frame.getWidth();
			}
			
			bufferFrame.setLocation(xPos, frame.getY());
			bufferFrame.addWindowListener(new BufferWindowListener());
			
			//Set look and feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e4) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e4);
			} catch (InstantiationException e4) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e4);
			} catch (IllegalAccessException e4) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e4);
			} catch (UnsupportedLookAndFeelException e4) {
				int hour = ZonedDateTime.now().getHour();
				int min = ZonedDateTime.now().getMinute();
				int sec = ZonedDateTime.now().getSecond();
				System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e4);
			}
			
			bufferPanel = new JPanel();
			bufferPanel.setLayout(new BoxLayout(bufferPanel, BoxLayout.PAGE_AXIS));
			
			bottomPanel = new JPanel();
			clearButton = new JButton("Clear all");
			bottomPanel.add(clearButton);
			clearButton.addActionListener(new ClearButtonActionListener());
			
			buttons = new ArrayList<JButton>();
			
			for(String at: cannotGet) {
				JButton newButton = new JButton(at);
				newButton.addActionListener(new RemoveEpActionListener(at));
				bufferPanel.add(newButton);
				buttons.add(newButton);
			}
			
			bufferFrame.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.weightx = 1;
			gc.weighty = .98;
			gc.gridx = 0;
			gc.gridy = 0;
			gc.fill = GridBagConstraints.BOTH;
			JScrollPane scrollPane = new JScrollPane(bufferPanel);
			bufferFrame.add(scrollPane, gc);
			gc.weighty = .02;
			gc.gridy = 1;
			bufferFrame.add(bottomPanel, gc);
			
			bufferFrame.setVisible(true);
		}
		
		private class ClearButtonActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(bufferFrame, "Remove all episodes " + 
						"from the buffer?", "TV Show Episode Picker", JOptionPane.YES_NO_OPTION) ==
						JOptionPane.YES_OPTION) {
					for(JButton at: buttons) {
						((RemoveEpActionListener) at.getActionListeners()[0]).noConfirmRemove(at);
					}
				}
			}
		}
		
		private class RemoveEpActionListener implements ActionListener {
			private String epToRemove;
			
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(bufferFrame, "Remove " + epToRemove +
						" from the buffer?", "TV Show Episode Picker", JOptionPane.YES_NO_OPTION) ==
						JOptionPane.YES_OPTION) {
					cannotGet.remove(epToRemove);
					JButton thisButton = (JButton) e.getSource();
					noConfirmRemove(thisButton);
				}
			}
			
			public synchronized void noConfirmRemove(JButton button) {
				System.out.println("[DEBUG]: Commencing removal of " + epToRemove + ".");
				cannotGet.remove(epToRemove);
				System.out.println("         Removed from cannotGet");
				Container parent = button.getParent();
				parent.remove(button);
				parent.revalidate();
				parent.repaint();
				System.out.println("         Removed button");
				
				//actually remove it from the file
				try {
					System.out.println("[DEBUG]: Removing from file...");
					File tempFile = new File("temp.tmp");
					System.out.println("         Created new file");
					
					BufferedReader reader = new BufferedReader(new FileReader(file));
					BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
					
					String lineAt = reader.readLine();
					
					System.out.println("         Searching for cannot get section of file");
					while(lineAt != null && !lineAt.equals("Cannot get:")) {
						writer.write(lineAt + "\n");
						lineAt = reader.readLine();
					}
					System.out.println("         Found cannot get!");
					System.out.println("         Searching for episode to remove");
					while(lineAt != null) {
						if(!lineAt.equals(epToRemove)) {
							writer.write(lineAt + "\n");
						}
						lineAt = reader.readLine();
					}
					
					writer.close();
					reader.close();
					file.delete();
					tempFile.renameTo(file);
				} catch (FileNotFoundException e) {
					int hour = ZonedDateTime.now().getHour();
					int min = ZonedDateTime.now().getMinute();
					int sec = ZonedDateTime.now().getSecond();
					System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + e);
					results.append("File " + file.getName() + " not found!\n");
					fileLoaded = false;
				} catch(IOException ioe) {
					int hour = ZonedDateTime.now().getHour();
					int min = ZonedDateTime.now().getMinute();
					int sec = ZonedDateTime.now().getSecond();
					System.err.println("[" + hour + ":" + min + ":" + sec + "]: " + ioe);
					results.append("IOException in reading " + file.getName() + ".\n"
							+ "This means it is not a valid file.\n" +
							"Please load a valid file.\n");
					fileLoaded = false;
				}
			}
			
			public RemoveEpActionListener(String ep) {
				epToRemove = ep;
			}	
		}
		
		private class BufferWindowListener implements WindowListener {

			public void windowClosing(WindowEvent e) {
				openedBufferPanel = false;	
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowActivated(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}
			
			public void windowOpened(WindowEvent e) {
			}
		}
	}

}
