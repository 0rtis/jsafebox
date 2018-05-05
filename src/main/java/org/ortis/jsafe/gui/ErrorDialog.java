
package org.ortis.jsafe.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ortis.jsafe.Utils;
import org.ortis.jsafe.gui.preview.ErrorPreview;

public class ErrorDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErrorDialog(final SafeExplorer safeExplorer, final String message, final Exception exception)
	{

		super(safeExplorer.getExplorerFrame(), "Error", true);

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(ErrorPreview.class.getResource("/img/icons8-high-priority-filled-50.png")));
		add(lblNewLabel, BorderLayout.WEST);

		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textPane.setOpaque(false);
		textPane.setEditable(false);
		final String msg = (message == null ? "" : message) + "\n" + Utils.formatException(exception);
		textPane.setText(msg);
		textPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBorder(new EmptyBorder(50, 30, 0, 0));
		textPane.setCaretPosition(0);
		add(scrollPane, BorderLayout.CENTER);

		JLabel lblNewLabel_1 = new JLabel("Error");
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNewLabel_1, BorderLayout.NORTH);

		final JFrame parentFrame = safeExplorer.getExplorerFrame();
		setPreferredSize(new Dimension(400, 400));
		setMaximumSize(new Dimension(parentFrame.getWidth() * 2 / 3, parentFrame.getHeight() * 2 / 3));
		setMinimumSize(new Dimension(parentFrame.getWidth() / 2, parentFrame.getHeight() / 2));
		setSize(getPreferredSize());
		setLocationRelativeTo(parentFrame);
	}

}
