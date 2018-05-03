
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.ortis.jsafe.Folder;
import org.ortis.jsafe.Safe;
import org.ortis.jsafe.SafeFile;
import org.ortis.jsafe.Utils;
import javax.swing.JProgressBar;

public class SafeExplorer
{

	private JFrame frmJsafe;
	private JTable propertyTable;

	private JTree tree;
	private DefaultTreeModel treeModel;
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

	private JProgressBar progressBar;

	private Safe safe;

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
					window.frmJsafe.setVisible(true);
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

		// show the file system roots.
		root.removeAllChildren();

		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Safe");
		root.add(node);
		for (final SafeFile safeFile : safe.getRootFolder().listFiles())
		{
			
			node.add(new DefaultMutableTreeNode(safeFile));
			if (safeFile.isFolder())
			{

				// node.add(new DefaultMutableTreeNode(sa));
			}

		}

		treeModel.reload();

		tree.expandRow(0);
	}

	private void loadNode(final DefaultMutableTreeNode node)
	{

		tree.setEnabled(false);
		progressBar.setVisible(true);
		// progressBar.setIndeterminate(true);

		SwingWorker<Void, SafeFile> worker = new SwingWorker<Void, SafeFile>()
		{
			@Override
			public Void doInBackground()
			{
				SafeFile file = (SafeFile) node.getUserObject();
				if (file.isFolder())
				{
					final Folder folder = (Folder) file;
					// if (node.isLeaf())
					{
						for (SafeFile sf : folder.listFiles())
						{
							// if (sf.isFolder())
							{
								publish(sf);
							}
						}
					}
					// setTableData(files);
				}
				return null;
			}

			@Override
			protected void process(List<SafeFile> chunks)
			{
				for (final SafeFile sf : chunks)
				{
					node.add(new DefaultMutableTreeNode(sf));
				}
			}

			@Override
			protected void done()
			{
				// progressBar.setIndeterminate(false);
				progressBar.setVisible(false);
				tree.setEnabled(true);
			}
		};
		worker.execute();

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

		frmJsafe = new JFrame();
		frmJsafe.setTitle("JSafe");

		final JPanel main = new JPanel(new BorderLayout(3, 3));
		main.setBackground(Color.WHITE);
		main.setBorder(new EmptyBorder(5, 5, 5, 5));

		frmJsafe.setContentPane(main);

		// main.setBackground(Color.WHITE);

		final JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.WHITE);
		final JScrollPane leftJscrollPane = new JScrollPane(leftPanel);
		leftPanel.setLayout(new BorderLayout(0, 0));

		

		treeModel = new DefaultTreeModel(root);

		TreeSelectionListener treeSelectionListener = new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent tse)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
				loadNode(node);
				// setFileDetails((File) node.getUserObject());
				System.out.println(node);
			}
		};

		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(treeSelectionListener);
		tree.setCellRenderer(new SafeFileRenderer());
		

		leftPanel.add(tree);
		final JPanel rightPanel = new JPanel(new BorderLayout(3, 3));

		JPanel previewPanel = new JPanel(new BorderLayout(3, 3));
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

		propertyTable = new JTable();
		propertyTable.setModel(new SafeFileTableModel(null));
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
		frmJsafe.getContentPane().add(splitPane);

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frmJsafe.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel(" Status");
		lblNewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(lblNewLabel, BorderLayout.WEST);

		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		progressBar.setEnabled(false);
		progressBar.setIndeterminate(true);
		panel.add(progressBar, BorderLayout.EAST);

		JMenuBar menuBar = new JMenuBar();
		frmJsafe.getContentPane().add(menuBar, BorderLayout.NORTH);
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorderPainted(false);
		menuBar.setMargin(new Insets(2, 0, 0, 0));

		JMenu mnFile = new JMenu("File");
		mnFile.setHorizontalAlignment(SwingConstants.CENTER);
		mnFile.setBackground(Color.WHITE);
		menuBar.add(mnFile);

		JMenuItem mntmAdd = new JMenuItem("Add");
		mntmAdd.setBackground(Color.WHITE);
		mntmAdd.setHorizontalAlignment(SwingConstants.LEFT);
		mnFile.add(mntmAdd);

		final List<Image> icons = new ArrayList<>();
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-16.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-32.png")));
		icons.add(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-64.png")));
		frmJsafe.setIconImages(icons);
		// frmJsafe.setIconImage(Toolkit.getDefaultToolkit().getImage(SafeExplorer.class.getResource("/img/icons8-safe-drawing-100.png")));
		frmJsafe.setBounds(100, 100, 909, 615);

		// frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width*3/5, Toolkit.getDefaultToolkit().getScreenSize().height*3/5);
		frmJsafe.setSize(Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3, Toolkit.getDefaultToolkit().getScreenSize().height * 2 / 3);

		frmJsafe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
