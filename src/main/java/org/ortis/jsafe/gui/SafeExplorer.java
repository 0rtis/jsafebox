
package org.ortis.jsafe.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

import org.ortis.jsafe.Block;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.Utils;
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

	private static final String CONFIG_AUTOSAVE_KEY = "gui.autosave";
	private static final String CONFIG_PREVIEW_KEY = "gui.preview";

	private final Properties config = new Properties();
	private JFrame explorerFrame;

	private JTable propertyTable;
	private JTree tree;

	private JRadioButtonMenuItem showPreview;
	private JRadioButtonMenuItem autoSave;
	private JProgressBar progressBar;
	private JPanel previewPanel;

	private Safe safe;

	private JLabel statusLabel;

	private AtomicBoolean modificationPending = new AtomicBoolean(false);

	/**
	 * Launch the application.
	 */
	public static void main(String [] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					SafeExplorer window = new SafeExplorer();
					window.explorerFrame.setVisible(true);
					window.setSafe(Utils.open("target/test", "password".toCharArray(), 1024, Logger.getAnonymousLogger()));

				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 */
	public SafeExplorer()
	{
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
					{/*
						node.removeAllChildren();
						final Folder folder = (Folder) file;
						//if (node.isLeaf())// if not children
							for (SafeFile sf : folder.listFiles())
								publish(sf);
						*/
					} else if (showPreview.isSelected())
					{
						// preview
						final Block block = (Block) file;

						for (final Map.Entry<String, String> metadata : block.getProperties().entrySet())
							propertyModel.addRow(new Object[] { metadata.getKey(), metadata.getValue() });

						final String mime = block.getProperties().get("content-type");
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

	public void configUI()
	{
		String conf = this.config.getProperty(CONFIG_AUTOSAVE_KEY);
		if (conf != null)
		{
			if (conf.toUpperCase(Locale.ENGLISH).equals("TRUE"))
				this.autoSave.setSelected(true);
			else if (conf.toUpperCase(Locale.ENGLISH).equals("FALSE"))
				this.autoSave.setSelected(false);
		}

		conf = this.config.getProperty(CONFIG_PREVIEW_KEY);
		if (conf != null)
		{
			if (conf.toUpperCase(Locale.ENGLISH).equals("TRUE"))
				this.showPreview.setSelected(true);
			else if (conf.toUpperCase(Locale.ENGLISH).equals("FALSE"))
				this.showPreview.setSelected(false);
		}

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
		explorerFrame.setTitle("JSafe");

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
					// setFileDetails((File) node.getUserObject());
					System.out.println(node);
				}
			}
		};

		tree = new JTree(new SafeTreeModel(this));
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(treeSelectionListener);
		tree.setCellRenderer(new SafeFileTreeCellRenderer());

		final SafeTreeCellEditor safeTreeCellEditor = new SafeTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer());
		tree.setCellEditor(safeTreeCellEditor);

		tree.setDragEnabled(true);
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
		toolBar.add(btnNewButton);

		JButton btnDelete = new JButton("");
		btnDelete.setIcon(new ImageIcon(SafeExplorer.class.getResource("/img/icons8-trash-20.png")));
		btnDelete.setToolTipText("Delete");
		btnDelete.setBackground(Color.WHITE);
		toolBar.add(btnDelete);
		splitPane.setResizeWeight(0.3);
		splitPane.setOneTouchExpandable(true);
		splitPane.setBackground(Color.WHITE);
		explorerFrame.getContentPane().add(splitPane);

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		explorerFrame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		statusLabel = new JLabel(" Status");
		statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
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
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK);
		autoSave.setAccelerator(keyStrokeAccelerator);
		autoSave.setSelected(true);
		autoSave.setToolTipText("Automatically save the file after a modification");
		mnFile.add(autoSave);

		JMenu mnNewMenu = new JMenu("View");
		menuBar.add(mnNewMenu);

		showPreview = new JRadioButtonMenuItem("Show preview");
		showPreview.setSelected(true);
		keyStrokeAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK);
		showPreview.setAccelerator(keyStrokeAccelerator);

		mnNewMenu.add(showPreview);

		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-64.png")));
		explorerFrame.setIconImages(icons);
		// frmJsafe.setIconImage(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-drawing-100.png")));
		// explorerFrame.setBounds(100, 100, 909, 615);
		// explorerFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width/2, Toolkit.getDefaultToolkit().getScreenSize().height/2);

		explorerFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);
		explorerFrame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - explorerFrame.getSize().width / 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 2 - explorerFrame.getSize().height / 2);

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
	public void actionPerformed(final ActionEvent e)
	{

		switch (e.getActionCommand())
		{
			case "Save":

				final ProgressDialog pd = new ProgressDialog(SafeExplorer.this);
				pd.setTitle("Saving...");
				final SaveTask saveTask = new SaveTask(SafeExplorer.this);
				pd.monitor(saveTask, "Saving safe...");

				break;

			default:
				break;
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

				new ErrorDialog(this, "Implement exit without saving frame", null).setVisible(true);
			}

			statusLabel.setText("Closing safe...");
			try
			{
				this.safe.close();
			} catch (final Exception e)
			{
				new ErrorDialog(this, "Error while closing safe", e).setVisible(true);

			}
		}

		this.explorerFrame.dispose();
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
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

	public Properties getConfig()
	{
		return config;
	}

}
