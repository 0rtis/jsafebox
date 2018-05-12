/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package org.ortis.jsafe.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.ortis.jsafe.Block;
import org.ortis.jsafe.Folder;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.gui.preview.ErrorPreview;
import org.ortis.jsafe.gui.preview.ImagePreview;
import org.ortis.jsafe.gui.tasks.SaveTask;
import org.ortis.jsafe.gui.tree.FileTransferHandler;
import org.ortis.jsafe.gui.tree.SafeFileNodePopupMenu;
import org.ortis.jsafe.gui.tree.SafeFileTreeCellRenderer;
import org.ortis.jsafe.gui.tree.SafeFileTreeNode;
import org.ortis.jsafe.gui.tree.SafeTreeCellEditor;
import org.ortis.jsafe.gui.tree.SafeTreeModel;

public class SafeExplorer implements WindowListener, ActionListener
{
	public final static String CONFIG_FILE = "jsafe.gui.properties";

	private final static String TITLE = "JSafe";
	private final Configuration configuration;

	private JFrame explorerFrame;

	private JTable propertyTable;
	private JTree tree;

	private JRadioButtonMenuItem showPreview;
	private JRadioButtonMenuItem autoSave;
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

	public void setSafe(final Safe safe)
	{
		this.modificationPending.set(false);
		this.safe = safe;

		final SafeTreeModel model = new SafeTreeModel(this);
		SafeFileTreeNode node = new SafeFileTreeNode(safe.getRootFolder());
		model.getRootMode().add(node);
		for (final SafeFile safeFile : safe.getRootFolder().listFiles())
			node.add(new SafeFileTreeNode(safeFile));

		tree.setModel(model);
		model.reload();
		tree.expandRow(0);
		tree.repaint();

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		this.explorerFrame.setTitle(TITLE + " - " + safe.getFile().getAbsolutePath());
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
					if (file.isFolder())
					{
						// node.removeAllChildren();
						final Folder folder = (Folder) file;
						if (node.isLeaf())// if not children
							for (SafeFile sf : folder.listFiles())
								publish(sf);

					} else
					{
						// preview
						final Block block = (Block) file;

						for (final Map.Entry<String, String> metadata : block.getProperties().entrySet())
							propertyModel.addRow(new Object[] { metadata.getKey(), metadata.getValue() });

						final String mime = block.getProperties().get("content-type");
						if (showPreview.isSelected())
							if (mime != null)
							{
								if (mime.startsWith("text"))
								{
								} else if (mime.startsWith("image"))
								{

									try
									{

										final ByteArrayOutputStream baos = new ByteArrayOutputStream();
										safe.extract(block, baos);
										final ImagePreview imagePreview = new ImagePreview(ImageIO.read(new ByteArrayInputStream(baos.toByteArray())));

										previewPanel.add(imagePreview, BorderLayout.CENTER);
									} catch (final Exception e)
									{
										previewPanel.removeAll();
										previewPanel.add(new ErrorPreview(e), BorderLayout.CENTER);
									}

								} else
								{

									final String img;
									if (mime.startsWith("video"))
										img = "/img/icons8-video-file-100.png";
									else if (mime.startsWith("audio"))
										img = "/img/icons8-audio-file-100.png";
									else
										img = "/img/icons8-document-100.png";

									try
									{

										final ImagePreview imagePreview = new ImagePreview(ImageIO.read(SafeExplorer.class.getResourceAsStream(img)));

										previewPanel.add(imagePreview, BorderLayout.CENTER);
									} catch (final Exception e)
									{
										previewPanel.removeAll();
										previewPanel.add(new ErrorPreview(e), BorderLayout.CENTER);
									}

								}
							}

					}

					return null;

				} catch (final Exception e)
				{
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void process(List<SafeFile> chunks)
			{
				for (final SafeFile sf : chunks)
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

	private void configUI()
	{

		this.autoSave.setSelected(this.configuration.getAutoSave());
		this.showPreview.setSelected(this.configuration.getPreview());

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e)
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
				if (node.getParent() != null)
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
				if (selRow != -1)
				{

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

					if (node.getParent() != null)
					{
						final SafeFileTreeNode sfNode = (SafeFileTreeNode) node;

						if (e.getClickCount() == 2)
						{

							System.out.println(node.getUserObject());
						}
						if (SwingUtilities.isRightMouseButton(e))
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

		final DefaultTableModel tableModel = new DefaultTableModel(new Object[0][0], new String[] { "Property", "Value" })
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
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-64.png")));
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

		if (event.getActionCommand().equals("Save"))
		{
			final ProgressDialog pd = new ProgressDialog(this.explorerFrame);
			pd.setTitle("Saving safe...");
			final SaveTask saveTask = new SaveTask(SafeExplorer.this);
			pd.monitor(saveTask, "Saving safe...");

		} else if (event.getActionCommand().equals(this.autoSave.getActionCommand()))
			this.configuration.setAutoSave(this.autoSave.isSelected());
		else if (event.getActionCommand().equals(this.showPreview.getActionCommand()))
			this.configuration.setPreview(this.showPreview.isSelected());
		else if (event.getActionCommand().equals(this.helpFrameMenuItem.getActionCommand()))
		{
			try
			{
				final HelpFrame helpFrame = new HelpFrame(this);
				helpFrame.setVisible(true);
			} catch (final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while opening help frame", e).setVisible(true);
			}

		} else if (event.getActionCommand().equals(this.aboutMenuItem.getActionCommand()))
		{
			try
			{
				final AboutFrame aboutFrame = new AboutFrame(this);
				aboutFrame.setVisible(true);
			} catch (final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while opening about frame", e).setVisible(true);
			}

		}

	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(final WindowEvent event)
	{

		if (this.safe != null)
		{

			if (modificationPending.get())
			{

				final JOptionPane optionPane = new JOptionPane("Exit without saving ?", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				final JDialog dialog = optionPane.createDialog(this.explorerFrame, "Warning");
				dialog.addKeyListener(new KeyAdapter()
				{
					public void keyPressed(KeyEvent ke)
					{ // handler
						if (ke.getKeyCode() == KeyEvent.VK_ESCAPE)
							dialog.dispose();
					}
				});

				dialog.setVisible(true);

				final Integer action = (Integer) optionPane.getValue();

				if (action == null || action != JOptionPane.YES_OPTION)
					return;

			}

			statusLabel.setText("Closing safe...");
			try
			{
				this.safe.close();
			} catch (final Exception e)
			{
				new ErrorDialog(this.explorerFrame, "Error while closing safe", e).setVisible(true);

			}
		}

		this.explorerFrame.dispose();

	}

	@Override
	public void windowClosed(final WindowEvent event)
	{
		try
		{
			this.configuration.store(new FileOutputStream(CONFIG_FILE), "Safe Explorer settings");
		} catch (final IOException e)
		{
			e.printStackTrace();
		}

		System.exit(0);
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
