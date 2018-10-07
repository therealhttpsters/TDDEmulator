// 
// Copyright 2012 Jeff Bush
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

class TTYEmulator extends JPanel
{
	public TTYEmulator()
	{
		super(new BorderLayout());

		fConversationView = new JTextPane();
		fConversationView.setEditable(false);	
		StyledDocument doc = fConversationView.getStyledDocument();		
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(regular, "Serif");
		Style me = doc.addStyle("me", regular);
		StyleConstants.setBold(me, true);
		StyleConstants.setForeground(me, Color.DARK_GRAY);
		Style them = doc.addStyle("them", regular);
		
		JScrollPane conversationScroll = new JScrollPane(fConversationView);
		add(conversationScroll, BorderLayout.CENTER);

		fInputField = new JTextField();
		fInputField.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e)
				{
					handleTextInput();
				}
			});
		add(fInputField, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(400,300));

		fOutput = new TTYOutput();
		fInput = new TTYInput();
		
		fInput.setListener(new TTYInput.TTYInputListener() {
			public void handleCode(char ch) {
				addReceivedCharacter(ch);
			}
		});

		fOutput.setListener(new TTYOutput.TTYOutputListener() {
			public void ttyIsSending(boolean isSending) {
				// If we are actively sending, disable our receiver so 
				// we don't echo characters.
				fInput.setIgnoreInput(isSending);
			}
		});
	}

	void handleTextInput()
	{
		String input = fInputField.getText() + "\n";
		fInputField.setText("");
	
		try
		{
			StyledDocument doc = fConversationView.getStyledDocument();
			if (fUnterminatedInputLine)
			{
				// If the remote user was in the middle of a line, add a 
				// line break here so the conversations don't get mixed.
				doc.insertString(doc.getLength(), "\n", doc.getStyle("them"));
			}

			doc.insertString(doc.getLength(), input, doc.getStyle("me"));
		}
		catch (Exception exc)
		{
			System.out.println(exc);
		}

		fOutput.enqueueString(input);
	}
	
	void addReceivedCharacter(char ch)
	{
		fUnterminatedInputLine = (ch != '\n' && ch != '\r');

		try
		{
			StyledDocument doc = fConversationView.getStyledDocument();
			doc.insertString(doc.getLength(), "" + ch, doc.getStyle("them"));
		}
		catch (Exception exc)
		{
			System.out.println(exc);
		}
	}

	private static void createAndShowGUI() 
	{
		JFrame frame = new JFrame("TTY Emulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		TTYEmulator emulator = new TTYEmulator();
		frame.getContentPane().add(emulator);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		}); 
	}
	
	private boolean fUnterminatedInputLine = false;
	private TTYOutput fOutput;
	private TTYInput fInput;
	private JTextPane fConversationView;
	private JTextField fInputField;
}
