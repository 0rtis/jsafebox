/*
 *  Copyright 2019 Ortis (ortis@ortis.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.ortis.jsafebox.gui.old;

import io.ortis.jsafebox.Block;
import io.ortis.jsafebox.Folder;
import io.ortis.jsafebox.Safe;
import io.ortis.jsafebox.SafeFile;
import io.ortis.jsafebox.gui.old.previews.ErrorPreview;
import io.ortis.jsafebox.gui.old.previews.ImagePreview;
import io.ortis.jsafebox.gui.old.previews.TextPreview;
import io.ortis.jsafebox.gui.old.tasks.HashTask;
import io.ortis.jsafebox.gui.old.tasks.SaveTask;
import io.ortis.jsafebox.gui.old.tree.*;
import io.ortis.jsafebox.gui.old.viewers.ImageViewer;
import io.ortis.jsafebox.gui.old.viewers.TextViewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SafeExplorer implements WindowListener, ActionListener
{
	public final static String CONFIG_FILE = "jsafebox.gui.properties";
	public static final DecimalFormat MEMORY_FORMAT = new DecimalFormat("###,###");
	private final static String TITLE = "JSafebox";
	private final static long TEXT_DISPLAY_MAX_LENGTH = 1_000_000;
	private final Configuration configuration;

	private final List<JFrame> frames = new ArrayList<>();

	private JFrame explorerFrame;

	private JTable propertyTable;
	private JTree tree;

	private JRadioButtonMenuItem showPreview;
	private JRadioButtonMenuItem autoSave;
	private JRadioButtonMenuItem autoHashCheck;

	private JMenuItem checkHashMenuItem;

	private JMenuItem helpFrameMenuItem;
	private JMenuItem aboutMenuItem;

	private JProgressBar progressBar;
	private JPanel previewPanel;

	private Safe safe;

	private JLabel statusLabel;

	private AtomicBoolean modificationPending = new AtomicBoolean(false);

	/**
	 * Create the application.
	 */
	public SafeExplorer(final Configuration configuration)
	{
		this.configuration = configuration;
		initialize();
	}

	private void loadNode(final SafeFileTreeNode node)
	{
		progressBar.setIndeterminate(true);
		progressBar.setVisible(true);

		SwingWorker<Void, SafeFile> worker = new SwingWorker<Void, SafeFile>()
		{
			@Override
			public Void doInBackground()
			{
				try
				{
					// clear preview
					previewPanel.removeAll();

					// clear properties
					final DefaultTableModel propertyModel = (DefaultTableModel) propertyTable.getModel();
					propertyModel.getDataVector().removeAllElements();
					propertyModel.fireTableDataChanged();

					SafeFile file = (SafeFile) node.getUserObject();
					if(file.isFolder())
					{
						// node.removeAllChildren();
						final Folder folder = (Folder) file;
						if(node.isLeaf())// if not children
							for(SafeFile sf : folder.listFiles())
								publish(sf);

					}
					else
					{
						// preview
						final Block block = (Block) file;

						final List<String> displayed = new ArrayList<>();

						propertyModel.addRow(new Object[]{Block.NAME_LABEL, block.getProperties().get(Block.NAME_LABEL)});
						propertyModel.addRow(new Object[]{Block.PATH_LABEL, block.getProperties().get(Block.PATH_LABEL)});
						propertyModel.addRow(new Object[]{"size", block.getLength() > 1000 ? MEMORY_FORMAT.format(
								block.getLength() / 1000) + " Kb" : block.getLength() + " bytes"});

						for(int i = 0; i < propertyModel.getRowCount(); i++)
							displayed.add(propertyModel.getValueAt(i, 0).toString());

						for(final Map.Entry<String, String> metadata : block.getProperties().entrySet())
							if(!displayed.contains(metadata.getKey()))
								propertyModel.addRow(new Object[]{metadata.getKey(), metadata.getValue()});

						final String mime = block.getProperties().get(Block.MIME_LABEL);
						if(showPreview.isSelected())
							if(mime != null)
							{
								if(mime.startsWith("image"))
								{
									try
									{

										final ByteArrayOutputStream baos = new ByteArrayOutputStream();
										safe.extract(block,true, baos);
										final ImagePreview imagePreview = new ImagePreview(ImageIO.read(new ByteArrayInputStream(baos.toByteArray())));

										previewPanel.add(imagePreview, BorderLayout.CENTER);
									} catch(final Exception e)
									{
										previewPanel.removeAll();
										previewPanel.add(new ErrorPreview(e), BorderLayout.CENTER);
									}

								}
								else if(mime.startsWith("text"))
								{
									try
									{
										final ByteArrayOutputStream baos = new ByteArrayOutputStream();
										safe.extract(block,true, baos);
										final String text = new String(baos.toByteArray());// use local charset
										final TextPreview textPreview = new TextPreview(text);

										previewPanel.add(textPreview, BorderLayout.CENTER);
									} catch(final Exception e)
									{
										previewPanel.removeAll();
										previewPanel.add(new ErrorPreview(e), BorderLayout.CENTER);
									}

								}
								else
								{

									final String img;
									if(mime.startsWith("video"))
										img = "/img/icons8-video-file-100.png";
									else if(mime.startsWith("audio"))
										img = "/img/icons8-audio-file-100.png";
									else
										img = "/img/icons8-document-100.png";

									try
									{

										final ImagePreview imagePreview = new ImagePreview(ImageIO.read(SafeExplorer.class.getResourceAsStream(img)));

										previewPanel.add(imagePreview, BorderLayout.CENTER);
									} catch(final Exception e)
									{
										previewPanel.removeAll();
										previewPanel.add(new ErrorPreview(e), BorderLayout.CENTER);
									}
								}
							}
					}

					return null;

				} catch(final Exception e)
				{
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void process(List<SafeFile> chunks)
			{
				for(final SafeFile sf : chunks)
					node.add(new SafeFileTreeNode(sf));
			}

			@Override
			protected void done()
			{
				progressBar.setVisible(false);
				progressBar.setIndeterminate(false);
				tree.repaint();
				previewPanel.repaint();

			}
		};
		worker.execute();

	}

	private void openNode(final SafeFileTreeNode node)
	{
		SafeFile file = (SafeFile) node.getUserObject();
		if(file.isBlock())
		{
			final Block block = (Block) file;

			final String mime = block.getProperties().get(Block.MIME_LABEL);
			if(mime != null)
				if(mime.startsWith("image"))
				{
					try
					{
						final ImageViewer viewer = new ImageViewer(safe, block, explorerFrame.getTitle() + " - ");
						viewer.addWindowListener(SafeExplorer.this);
						viewer.setVisible(true);

					} catch(final Exception exception)
					{
						new ErrorDialog(explorerFrame, "Error while opening text viewer", exception).setVisible(true);
					}
				}
				else if(mime.startsWith("text") || block.getDataLength() < TEXT_DISPLAY_MAX_LENGTH)// Display as text if mime text or less than TEXT_DISPLAY_MAX_LENGTH
				{
					try
					{
						final ByteArrayOutputStream baos = new ByteArrayOutputStream();
						safe.extract(block,true, baos);
						final String text = new String(baos.toByteArray());// Use local Charset
						final TextViewer viewer = new TextViewer(text);
						viewer.setTitle(explorerFrame.getTitle() + " - " + block.getPath());
						viewer.addWindowListener(SafeExplorer.this);
						viewer.setVisible(true);

					} catch(final Exception exception)
					{
						new ErrorDialog(explorerFrame, "Error while opening text viewer", exception).setVisible(true);
					}

				}
		}
	}

	private void configUI()
	{

		this.autoSave.setSelected(this.configuration.getAutoSave());
		this.showPreview.setSelected(this.configuration.getPreview());
		this.autoHashCheck.setSelected(this.configuration.getAutoHashCheck());

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(final Exception e)
		{
		}

		explorerFrame = new JFrame();
		explorerFrame.setTitle(TITLE);

		final JPanel main = new JPanel(new BorderLayout(3, 3));
		main.setBackground(Color.WHITE);
		main.setBorder(new EmptyBorder(5, 5, 5, 5));

		explorerFrame.setContentPane(main);

		// main.setBackground(Color.WHITE);

		final JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.WHITE);
		final JScrollPane leftJscrollPane = new JScrollPane(leftPanel);
		leftPanel.setLayout(new BorderLayout(0, 0));

		TreeSelectionListener treeSelectionListener = new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent tse)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
				if(node.getParent() != null)
				{
					loadNode((SafeFileTreeNode) node);
				}
			}
		};

		tree = new JTree(new SafeTreeModel(this));
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(treeSelectionListener);
		tree.setCellRenderer(new SafeFileTreeCellRenderer());

		final SafeTreeCellEditor safeTreeCellEditor = new SafeTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer());
		tree.setCellEditor(safeTreeCellEditor);

		// tree.setDragEnabled(true);
		tree.setTransferHandler(new FileTransferHandler());

		final MouseListener ml = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				final int selRow = tree.getRowForLocation(e.getX(), e.getY());
				final TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1)
				{

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

					if(node.getParent() != null)
					{
						final SafeFileTreeNode sfNode = (SafeFileTreeNode) node;

						if(e.getClickCount() == 2)
						{
							openNode(sfNode);
						}
						else if(SwingUtilities.isRightMouseButton(e))
						{
							tree.setSelectionRow(selRow);
							final SafeFileNodePopupMenu menu = new SafeFileNodePopupMenu(tree, sfNode);
							menu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			}
		};
		tree.addMouseListener(ml);

		final KeyListener kl = new KeyListener()
		{

			@Override
			public void keyTyped(final KeyEvent e)
			{
				if(e.getKeyChar() == '\n')
				{

					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
					if(node.getParent() != null)
					{
						final SafeFileTreeNode sfNode = (SafeFileTreeNode) node;
						openNode(sfNode);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
			}
		};

		tree.addKeyListener(kl);
		tree.setEditable(true);

		leftPanel.add(tree);
		final JPanel rightPanel = new JPanel(new BorderLayout(3, 3));

		previewPanel = new JPanel(new BorderLayout(3, 3));
		previewPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		rightPanel.add(previewPanel, BorderLayout.CENTER);
		previewPanel.setLayout(new BorderLayout(0, 0));
		previewPanel.setPreferredSize(new Dimension(450, 200));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftJscrollPane, rightPanel);

		JPanel infoPanel = new JPanel();
		infoPanel.setBackground(Color.WHITE);
		rightPanel.add(infoPanel, BorderLayout.SOUTH);
		infoPanel.setLayout(new BorderLayout(0, 0));
		infoPanel.setPreferredSize(new Dimension(450, 200));

		final DefaultTableModel tableModel = new DefaultTableModel(new Object[0][0], new String[]{"Property", "Value"})
		{
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		propertyTable = new JTable(tableModel);
		propertyTable.getTableHeader().setOpaque(false);

		final JScrollPane propertyTableScrollPane = new JScrollPane(propertyTable);
		propertyTableScrollPane.getViewport().setBackground(Color.WHITE);

		propertyTableScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		infoPanel.add(propertyTableScrollPane, BorderLayout.CENTER);

		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		toolBar.setBackground(Color.WHITE);
		infoPanel.add(toolBar, BorderLayout.NORTH);

		JButton btnNewButton = new JButton("");
		btnNewButton.setToolTipText("Extract");
		btnNewButton.setIcon(new ImageIcon(SafeExplorer.class.getResource("/img/icons8-downloading-updates-20.png")));
		btnNewButton.setBackground(Color.WHITE);
		btnNewButton.setVisible(false);
		toolBar.add(btnNewButton);

		JButton btnDelete = new JButton("");
		btnDelete.setIcon(new ImageIcon(SafeExplorer.class.getResource("/img/icons8-trash-20.png")));
		btnDelete.setToolTipText("Delete");
		btnDelete.setBackground(Color.WHITE);
		btnDelete.setVisible(false);
		toolBar.add(btnDelete);
		splitPane.setResizeWeight(0.3);
		splitPane.setOneTouchExpandable(true);
		splitPane.setBackground(Color.WHITE);
		explorerFrame.getContentPane().add(splitPane);

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		explorerFrame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		statusLabel = new JLabel("Status");
		statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		new StatusUpdater(statusLabel).start();

		panel.add(statusLabel, BorderLayout.WEST);

		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		progressBar.setEnabled(false);
		progressBar.setIndeterminate(true);
		panel.add(progressBar, BorderLayout.EAST);

		JMenuBar menuBar = new JMenuBar();
		explorerFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorderPainted(false);
		menuBar.setMargin(new Insets(2, 0, 0, 0));

		JMenu mnFile = new JMenu("File");
		mnFile.setHorizontalAlignment(SwingConstants.CENTER);
		mnFile.setBackground(Color.WHITE);
		menuBar.add(mnFile);

		JMenuItem saveMenuItem = new JMenuItem("Save");
		KeyStroke keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		saveMenuItem.setAccelerator(keyStrokeAccelerator);
		saveMenuItem.addActionListener(this);
		mnFile.add(saveMenuItem);

		autoSave = new JRadioButtonMenuItem("Auto save");
		autoSave.addActionListener(this);
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK);
		autoSave.setAccelerator(keyStrokeAccelerator);
		autoSave.setSelected(true);
		autoSave.setToolTipText("Automatically save the file after a modification");
		mnFile.add(autoSave);

		JMenu mnNewMenu = new JMenu("View");
		menuBar.add(mnNewMenu);

		showPreview = new JRadioButtonMenuItem("Show preview");
		showPreview.addActionListener(this);
		showPreview.setSelected(true);
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK);
		showPreview.setAccelerator(keyStrokeAccelerator);

		mnNewMenu.add(showPreview);

		JMenu mnSecurity = new JMenu("Security");
		menuBar.add(mnSecurity);

		checkHashMenuItem = new JMenuItem("Check hash...");
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
		checkHashMenuItem.setAccelerator(keyStrokeAccelerator);
		checkHashMenuItem.addActionListener(this);
		mnSecurity.add(checkHashMenuItem);

		autoHashCheck = new JRadioButtonMenuItem("Check hash on opening");

		autoHashCheck.addActionListener(this);
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK);
		autoHashCheck.setAccelerator(keyStrokeAccelerator);
		autoHashCheck.setSelected(true);
		autoHashCheck.setToolTipText("Check safe's hash before opening");
		mnSecurity.add(autoHashCheck);

		JMenu helpMenuItem = new JMenu("Help");
		menuBar.add(helpMenuItem);

		helpFrameMenuItem = new JMenuItem("Help frame...");
		helpFrameMenuItem.addActionListener(this);
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		helpFrameMenuItem.setAccelerator(keyStrokeAccelerator);
		helpMenuItem.add(helpFrameMenuItem);

		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(this);
		helpMenuItem.add(aboutMenuItem);

		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-64.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/frame-icons/icons8-safe-100.png")));
		explorerFrame.setIconImages(icons);

		explorerFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		explorerFrame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - explorerFrame.getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - explorerFrame.getSize().height / 2);

		configUI();

		explorerFrame.addWindowListener(this);
		explorerFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public void notifyModificationPending()
	{
		modificationPending.set(true);
		((DefaultTreeModel) tree.getModel()).reload();
		tree.expandRow(0);
	}

	public boolean isModificationPending()
	{
		return modificationPending.get();
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{

		if(event.getActionCommand().equals("Save"))
		{
			final ProgressDialog pd = new ProgressDialog(this.explorerFrame);
			pd.setTitle("Saving safe...");
			final SaveTask saveTask = new SaveTask(SafeExplorer.this);
			pd.monitor(saveTask, "Saving safe...");

		}
		else if(event.getActionCommand().equals(this.autoSave.getActionCommand()))
			this.configuration.setAutoSave(this.autoSave.isSelected());
		else if(event.getActionCommand().equals(this.showPreview.getActionCommand()))
			this.configuration.setPreview(this.showPreview.isSelected());
		else if(event.getActionCommand().equals(this.autoHashCheck.getActionCommand()))
			this.configuration.setAutoHashCheck(this.autoHashCheck.isSelected());
		else if(event.getActionCommand().equals(this.helpFrameMenuItem.getActionCommand()))
		{
			try
			{
				final HelpFrame helpFrame = new HelpFrame(this);
				helpFrame.setVisible(true);
			} catch(final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while opening help frame", e).setVisible(true);
			}

		}
		else if(event.getActionCommand().equals(this.aboutMenuItem.getActionCommand()))
		{
			try
			{
				final AboutFrame aboutFrame = new AboutFrame(this);
				aboutFrame.setVisible(true);
			} catch(final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while opening about frame", e).setVisible(true);
			}

		}
		else if(event.getActionCommand().equals(this.checkHashMenuItem.getActionCommand()))
		{
			try
			{

				final ProgressDialog hashCheckDialog = new ProgressDialog(this.explorerFrame);
				hashCheckDialog.setTitle("Integrity check");

				final HashTask hashTask = new HashTask(this.safe);
				hashCheckDialog.monitor(hashTask, "Computing hash...");

				final String expectedHash = DatatypeConverter.printHexBinary(this.safe.getHash());
				if(hashTask.getHash() != null)
					if(!hashTask.getHash().equals(expectedHash))
					{

						Object[] options = {"Yes (NOT RECOMMENDED)", "No"};
						final int selectedOptionId = JOptionPane.showOptionDialog(this.explorerFrame,
								"<html><div><b>CARREFULLY READ THIS MESSAGE !!!!!!!!!</b></div><br><div>The hash of the safe is </div><div><b>" + hashTask.getHash() + "</b></div><div>but was expected to be</div><div><b>" + expectedHash + "</b></div><br/><div>The content of the file might have been altered. It is strongly advised to revert to a backup file</div><br/><div>Do you want to continue with the current file ?</div><html>",
								"Integrity check failed", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);

						if(selectedOptionId != 0)
							this.explorerFrame.dispatchEvent(new WindowEvent(this.explorerFrame, WindowEvent.WINDOW_CLOSING));

					}
					else
						JOptionPane.showMessageDialog(this.explorerFrame,
								"<html><div>Integrity hash</div><br/><div><b>" + expectedHash + "</b><div><br/><div>sucessfully check !<div>",
								"Integrity check sucessful", JOptionPane.INFORMATION_MESSAGE);

			} catch(final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while check integrity hash", e).setVisible(true);
			}

		}

	}

	@Override
	public void windowOpened(WindowEvent e)
	{
		final JFrame jframe = (JFrame) e.getSource();
		this.frames.add(jframe);

	}

	@Override
	public void windowClosing(final WindowEvent event)
	{

		if(event.getSource() == this.explorerFrame)
		{
			if(this.safe != null)
			{

				if(modificationPending.get())
				{

					final JOptionPane optionPane = new JOptionPane("Exit without saving ?", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
					final JDialog dialog = optionPane.createDialog(this.explorerFrame, "Warning");
					dialog.addKeyListener(new KeyAdapter()
					{
						public void keyPressed(KeyEvent ke)
						{ // handler
							if(ke.getKeyCode() == KeyEvent.VK_ESCAPE)
								dialog.dispose();
						}
					});

					dialog.setVisible(true);

					final Integer action = (Integer) optionPane.getValue();

					if(action == null || action != JOptionPane.YES_OPTION)
						return;

				}

			}

			this.explorerFrame.dispose();
		}

	}

	@Override
	public void windowClosed(final WindowEvent event)
	{
		try
		{
			this.configuration.store(new FileOutputStream(CONFIG_FILE), "Safe Explorer settings");
		} catch(final IOException e)
		{
			e.printStackTrace();
		}

		final JFrame frame = (JFrame) event.getSource();
		frames.remove(frame);

		if(frames.size() == 0)
		{

			statusLabel.setText("Closing safe...");
			try
			{
				this.safe.close();
			} catch(final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while closing safe", e).setVisible(true);

			}

			System.out.println("Exiting.");
			System.exit(0);

		}
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	public Safe getSafe()
	{
		return safe;
	}

	public void setSafe(final Safe safe)
	{
		this.modificationPending.set(false);
		this.safe = safe;

		final SafeTreeModel model = new SafeTreeModel(this);
		SafeFileTreeNode node = new SafeFileTreeNode(safe.getRootFolder());
		model.getRootMode().add(node);
		for(final SafeFile safeFile : safe.getRootFolder().listFiles())
			node.add(new SafeFileTreeNode(safeFile));

		tree.setModel(model);
		model.reload();
		tree.expandRow(0);
		tree.repaint();

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		this.explorerFrame.setTitle(TITLE + " - " + safe.getFile().getAbsolutePath());
	}

	public JFrame getExplorerFrame()
	{
		return explorerFrame;
	}

	public JRadioButtonMenuItem getAutoSave()
	{
		return autoSave;
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}
}
