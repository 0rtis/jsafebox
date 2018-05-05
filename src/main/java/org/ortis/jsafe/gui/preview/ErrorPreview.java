
package org.ortis.jsafe.gui.preview;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.ortis.jsafe.Utils;

public class ErrorPreview extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErrorPreview(final Exception exception)
	{
		super(new BorderLayout());

		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(ErrorPreview.class.getResource("/img/icons8-high-priority-filled-50.png")));
		add(lblNewLabel, BorderLayout.WEST);

		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textPane.setOpaque(false);
		textPane.setEditable(false);
		final String msg = Utils.formatException(exception);
		textPane.setText(msg);
		textPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBorder(new EmptyBorder(50, 30, 0, 0));

		add(scrollPane, BorderLayout.CENTER);

		JLabel lblNewLabel_1 = new JLabel("Error");
		lblNewLabel_1.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNewLabel_1, BorderLayout.NORTH);
	}

}
