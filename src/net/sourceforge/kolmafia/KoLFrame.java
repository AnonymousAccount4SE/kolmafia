/**
 * Copyright (c) 2005, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia;

// containers
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JEditorPane;

// layout
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;

// event listeners
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import javax.swing.ListSelectionModel;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

// basic utilities
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLEncoder;
import java.lang.reflect.Method;

// other stuff
import javax.swing.SwingUtilities;
import edu.stanford.ejalbert.BrowserLauncher;

// spellcast imports
import net.java.dev.spellcast.utilities.LicenseDisplay;
import net.java.dev.spellcast.utilities.ActionPanel;
import net.java.dev.spellcast.utilities.JComponentUtilities;
import net.java.dev.spellcast.utilities.LockableListModel;

/**
 * An extended <code>JFrame</code> which provides all the frames in
 * KoLmafia the ability to update their displays, given some integer
 * value and the message to use for updating.
 */

public abstract class KoLFrame extends javax.swing.JFrame implements KoLConstants
{
	static { System.setProperty( "SHARED_MODULE_DIRECTORY", "net/sourceforge/kolmafia/" ); };

	private static final String [] LICENSE_FILENAME = { "kolmafia-license.gif", "spellcast-license.gif", "browserlauncher-license.htm" };
	private static final String [] LICENSE_NAME = { "KoLmafia BSD", "Spellcast BSD", "BrowserLauncher" };

	private String frameName;
	protected boolean isEnabled;
	protected KoLmafia client;
	protected KoLPanel contentPanel;
	protected boolean isExecutingScript;

	protected JPanel compactPane;
	protected JLabel hpLabel, mpLabel, advLabel;
	protected JLabel meatLabel, closetLabel, drunkLabel;

	/**
	 * Constructs a new <code>KoLFrame</code> with the given title,
	 * to be associated with the given client.
	 */

	protected KoLFrame( KoLmafia client, String title )
	{
		super( title );

		this.client = client;
		this.isEnabled = true;

		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		addWindowListener( new LocationAdapter() );

		this.frameName = getClass().getName();
		this.frameName = frameName.substring( frameName.lastIndexOf( "." ) + 1 );

		if ( client != null )
		{
			String positionsSetting = client.getSettings().getProperty( "savePositions" );

			if ( positionsSetting.equals( "true" ) && client.getSettings().containsKey( frameName ) )
			{
				String [] location = client.getSettings().getProperty( frameName ).split( "," );
				setLocation( Integer.parseInt( location[0] ), Integer.parseInt( location[1] ) );
			}
			else
				setLocationRelativeTo( null );
		}

		if ( !(this instanceof LoginFrame || this instanceof AdventureFrame) )
			existingFrames.add( this );
	}

	public void dispose()
	{
		super.dispose();

		if ( !(this instanceof LoginFrame || this instanceof AdventureFrame) )
		{
			Object [] frames = existingFrames.toArray();

			for ( int i = frames.length - 1; i >= 0; --i )
				if ( frames[i] == this )
					existingFrames.remove(i);
		}
	}

	public String getFrameName()
	{	return frameName;
	}

	/**
	 * Method which adds a compact pane to the west side of the component.
	 * Note that this method can only be used if the KoLFrame on which it
	 * is called has not yet added any components.  If there are any added
	 * components, this method will do nothing.
	 */

	public void addCompactPane()
	{
		if ( getContentPane().getComponentCount() != 0 )
			return;

		JPanel compactPane = new JPanel();
		compactPane.setOpaque( false );
		compactPane.setLayout( new GridLayout( 14, 1 ) );

		compactPane.add( Box.createHorizontalStrut( 80 ) );

		compactPane.add( new JLabel( JComponentUtilities.getSharedImage( "hp.gif" ), JLabel.CENTER ) );
		compactPane.add( hpLabel = new JLabel( " ", JLabel.CENTER ) );

		compactPane.add( new JLabel( JComponentUtilities.getSharedImage( "mp.gif" ), JLabel.CENTER ) );
		compactPane.add( mpLabel = new JLabel( " ", JLabel.CENTER ) );

		compactPane.add( new JLabel( JComponentUtilities.getSharedImage( "meat.gif" ), JLabel.CENTER ) );
		compactPane.add( meatLabel = new JLabel( " ", JLabel.CENTER ) );

		compactPane.add( new JLabel( JComponentUtilities.getSharedImage( "closet.gif" ), JLabel.CENTER ) );
		compactPane.add( closetLabel = new JLabel( " ", JLabel.CENTER ) );

		compactPane.add( new JLabel( JComponentUtilities.getSharedImage( "hourglass.gif" ), JLabel.CENTER ) );
		compactPane.add( advLabel = new JLabel( " ",  JLabel.CENTER) );

		compactPane.add( new JLabel( JComponentUtilities.getSharedImage( "sixpack.gif" ), JLabel.CENTER ) );
		compactPane.add( drunkLabel = new JLabel( " ",  JLabel.CENTER) );

		compactPane.add( Box.createHorizontalStrut( 80 ) );

		this.compactPane = new JPanel();
		this.compactPane.setLayout( new BorderLayout() );
		this.compactPane.add( compactPane, BorderLayout.NORTH );

		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( this.compactPane, BorderLayout.WEST );
		(new StatusRefresher()).run();

		if ( client != null )
			client.getCharacterData().addKoLCharacterListener( new KoLCharacterAdapter( new StatusRefresher() ) );
	}

	protected class StatusRefresher implements Runnable
	{
		public void run()
		{
			KoLCharacter characterData = client == null ? new KoLCharacter( "UI Test" ) : client.getCharacterData();
			hpLabel.setText( characterData.getCurrentHP() + " / " + characterData.getMaximumHP() );
			mpLabel.setText( characterData.getCurrentMP() + " / " + characterData.getMaximumMP() );
			meatLabel.setText( df.format( characterData.getAvailableMeat() ) );
			closetLabel.setText( df.format( characterData.getClosetMeat() ) );
			advLabel.setText( String.valueOf( characterData.getAdventuresLeft() ) );
			drunkLabel.setText( String.valueOf( characterData.getInebriety() ) );
		}
	}

	/**
	 * Method used to set the current extended state of the
	 * frame.  In most KoLFrames, this defaults to iconified
	 * or normal, and maximizing is disabled.  However, request
	 * frames and profile frames display raw HTML - therefore,
	 * they need to be maximizable.
	 */

	public void setExtendedState( int state )
	{
		if ( this instanceof ChatFrame || this instanceof RequestFrame || state == ICONIFIED )
			super.setExtendedState( state );
		else
			super.setExtendedState( NORMAL );

	}

	/**
	 * Updates the display to reflect the given display state and
	 * to contain the given message.  Note that if there is no
	 * content panel, this method does nothing.
	 */

	public void updateDisplay( int displayState, String message )
	{
		if ( client != null )
			client.getLogStream().println( message );

		if ( contentPanel != null )
			contentPanel.setStatusMessage( isExecutingScript && displayState != ERROR_STATE ? DISABLED_STATE : displayState, message );

		switch ( displayState )
		{
			case ERROR_STATE:
				setEnabled( true );
				break;

			case DISABLED_STATE:
				setEnabled( false );
				break;

			case NOCHANGE:
				break;

			default:
				if ( !isExecutingScript )
					setEnabled( true );
				break;

		}
	}

	/**
	 * Utility method used to give the content panel for this
	 * <code>KoLFrame</code> focus.  Note that if the content
	 * panel is <code>null</code>< this method does nothing.
	 */

	public void requestFocus()
	{
		super.requestFocus();
		if ( contentPanel != null )
			contentPanel.requestFocus();
	}

	protected final JMenu addStatusMenu( JComponent menu )
	{
		JMenu statusMenu = new JMenu( "My KoL" );
		statusMenu.setMnemonic( KeyEvent.VK_M );
		menu.add( statusMenu );

		statusMenu.add( new DisplayFrameMenuItem( "Status Pane", KeyEvent.VK_S, CharsheetFrame.class ) );
		statusMenu.add( new DisplayFrameMenuItem( "Gear Changer", KeyEvent.VK_G, GearChangeFrame.class ) );
		statusMenu.add( new DisplayFrameMenuItem( "Item Manager", KeyEvent.VK_I, ItemManageFrame.class ) );

		return statusMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code>
	 * people menu to the given menu bar.  The default menu contains
	 * the ability to open chat, compose green messages, and read
	 * current mail.
	 */

	protected final JMenu addPeopleMenu( JComponent menu )
	{
		JMenu peopleMenu = new JMenu( "People" );
		peopleMenu.setMnemonic( KeyEvent.VK_P );
		menu.add( peopleMenu );

		peopleMenu.add( new InvocationMenuItem( "Chat of Loathing", KeyEvent.VK_C, client, "initializeChat" ) );
		peopleMenu.add( new DisplayFrameMenuItem( "Green Composer", KeyEvent.VK_G, GreenMessageFrame.class ) );
		peopleMenu.add( new DisplayFrameMenuItem( "IcePenguin Express", KeyEvent.VK_I, MailboxFrame.class ) );
		peopleMenu.add( new DisplayFrameMenuItem( "Propose Trade Offer", KeyEvent.VK_P, ProposeTradeFrame.class ) );
		peopleMenu.add( new DisplayFrameMenuItem( "View Pending Trades", KeyEvent.VK_V, PendingTradesFrame.class ) );
		peopleMenu.add( new DisplayFrameMenuItem( "Not-Holiday Giftings", KeyEvent.VK_N, GiftMessageFrame.class ) );

		return peopleMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code>
	 * scripting menu to the given menu bar.  The default menu contains
	 * the ability to load scripts.
	 */

	protected final JMenu addScriptMenu( JComponent menu )
	{
		JMenu scriptMenu = new JMenu( "Scripts" );
		scriptMenu.setMnemonic( KeyEvent.VK_S );

		scriptMenu.add( new LoadScriptMenuItem() );
		scriptMenu.add( new ToggleMacroMenuItem() );
		scriptMenu.add( new DisplayFrameMenuItem( "Access CLI Mode", KeyEvent.VK_A, CommandDisplayFrame.class ) );

		menu.add( scriptMenu );

		File scriptDirectory = new File( "scripts" );
		if ( !scriptDirectory.exists() )
			scriptDirectory.mkdirs();
		else
		{
			int addedScriptCount = 0;
			String currentScriptName;
			JMenuItem currentScript;

			File [] scriptList = scriptDirectory.listFiles();
			for ( int i = 0; i < scriptList.length; ++i )
			{
				if ( !scriptList[i].isDirectory() )
				{
					if ( addedScriptCount == 0 )
						scriptMenu.add( new JSeparator() );

					try
					{
						String [] pieces = scriptList[i].getCanonicalPath().split( "[\\\\/]" );
						scriptMenu.add( new LoadScriptMenuItem( (++addedScriptCount) + "  " + pieces[ pieces.length - 1 ], pieces[ pieces.length - 1 ] ) );
					}
					catch ( Exception e )
					{
					}
				}
			}
		}

		return scriptMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code>
	 * configuration menu to the given menu bar.  The default menu
	 * contains the ability to customize preferences (global if it
	 * is invoked before login, character-specific if after) and
	 * initialize the debugger.
	 *
	 * @param	menu	The <code>JMenuBar</code> to which the configuration menu will be attached
	 */

	protected final JMenu addConfigureMenu( JComponent menu )
	{
		JMenu configureMenu = new JMenu( "Configure" );
		configureMenu.setMnemonic( KeyEvent.VK_C );
		menu.add( configureMenu );

		configureMenu.add( new DisplayFrameMenuItem( "Preferences", KeyEvent.VK_P, OptionsFrame.class ) );
		configureMenu.add( new ToggleDebugMenuItem() );

		return configureMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code> Help
	 * menu to the given menu bar.  The default Help menu contains the
	 * copyright statement for <code>KoLmafia</code>.
	 *
	 * @param	menu	The <code>JMenuBar</code> to which the Help menu will be attached
	 */

	protected final JMenu addHelpMenu( JComponent menu )
	{
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic( KeyEvent.VK_H );
		menu.add( helpMenu );

		helpMenu.add( new DisplayFrameMenuItem( "About KoLmafia...", KeyEvent.VK_A, LicenseDisplay.class ) );
		helpMenu.add( new DisplayPageMenuItem( "KoLmafia Home", KeyEvent.VK_K, "http://kolmafia.sourceforge.net/" ) );
		helpMenu.add( new DisplayPageMenuItem( "End-User Manual", KeyEvent.VK_E, "http://kolmafia.sourceforge.net/manual.html" ) );
		helpMenu.add( new DisplayPageMenuItem( "Sourceforge Page", KeyEvent.VK_S, "https://sourceforge.net/project/showfiles.php?group_id=126572&package_id=138474" ) );
		helpMenu.add( new DisplayPageMenuItem( "Read Forum Thread", KeyEvent.VK_R, "http://forums.kingdomofloathing.com/viewtopic.php?t=19779" ) );

		return helpMenu;
	}

	/**
	 * Auxilary method used to enable and disable a frame.  By default,
	 * this attempts to toggle the enable/disable status on the core
	 * content panel.  It is advised that descendants override this
	 * behavior whenever necessary.
	 *
	 * @param	isEnabled	<code>true</code> if the frame is to be re-enabled
	 */

	public void setEnabled( boolean isEnabled )
	{
		this.isEnabled = isEnabled;

		if ( contentPanel != null )
			contentPanel.setEnabled( isEnabled );

	}

	/**
	 * Overrides the default isEnabled() method, because the setEnabled()
	 * method does not call the superclass's version.
	 *
	 * @return	Whether or not this KoLFrame is enabled.
	 */

	public boolean isEnabled()
	{	return isEnabled;
	}

	private class ToggleMacroMenuItem extends JMenuItem implements ActionListener
	{
		public ToggleMacroMenuItem()
		{
			super( "", KeyEvent.VK_R );
			addActionListener( this );

			setText( client == null || client.getMacroStream() instanceof NullStream ? "Record Script..." : "Stop Recording" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if ( client != null && client.getMacroStream() instanceof NullStream )
			{
				JFileChooser chooser = new JFileChooser( "scripts" );
				int returnVal = chooser.showSaveDialog( KoLFrame.this );

				if ( chooser.getSelectedFile() == null )
					return;

				String filename = chooser.getSelectedFile().getAbsolutePath();

				if ( client != null && returnVal == JFileChooser.APPROVE_OPTION )
					client.initializeMacroStream( filename );

				setText( "Stop Recording" );
			}
			else if ( client != null )
			{
				client.deinitializeMacroStream();
				setText( "Record Script..." );
			}
		}
	}

	/**
	 * Internal class which attempts to create a menu item
	 * which toggles the text pending on current debug state.
	 */

	private class ToggleDebugMenuItem extends JMenuItem implements ActionListener
	{
		public ToggleDebugMenuItem()
		{
			super( "", KeyEvent.VK_S );
			addActionListener( this );

			setText( client == null || client.getLogStream() instanceof NullStream ? "Start Debug" : "Stop Debug" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if ( client != null && client.getLogStream() instanceof NullStream )
			{
				client.initializeLogStream();
				setText( "Stop Debug" );
			}
			else if ( client != null )
			{
				client.deinitializeLogStream();
				setText( "Start Debug" );
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for loading a script.
	 */

	private class LoadScriptMenuItem extends JMenuItem implements ActionListener
	{
		private String scriptPath;

		public LoadScriptMenuItem()
		{	this( "Load script...", "" );
		}

		public LoadScriptMenuItem( String scriptName, String scriptPath )
		{
			super( scriptName, KeyEvent.VK_L );
			addActionListener( this );

			this.scriptPath = scriptPath;
		}

		public void actionPerformed( ActionEvent e )
		{	(new LoadScriptThread()).start();
		}

		private class LoadScriptThread extends DaemonThread
		{
			public void run()
			{
				String executePath = scriptPath;

				try
				{
					if ( scriptPath == null )
					{
						JFileChooser chooser = new JFileChooser( "scripts" );
						int returnVal = chooser.showOpenDialog( KoLFrame.this );

						if ( chooser.getSelectedFile() == null )
							return;

						if ( client != null && returnVal == JFileChooser.APPROVE_OPTION )
							executePath = chooser.getSelectedFile().getCanonicalPath();
					}

					if ( executePath == null )
						return;

					isExecutingScript = true;
					(new KoLmafiaCLI( client, executePath )).listenForCommands();
				}
				catch ( Exception e )
				{
					// Here, notify the display that the script
					// file specified could not be loaded

					isExecutingScript = false;
					updateDisplay( ERROR_STATE, "Script <" + executePath + "> encountered an error." );
					return;
				}

				isExecutingScript = false;
				if ( client.permitsContinue() )
					updateDisplay( ENABLED_STATE, "Script completed successfully." );
				else
					updateDisplay( ERROR_STATE, "Script <" + executePath + "> encountered an error." );
			}
		}
	}

	/**
	 * A generic panel which adds a label to the bottom of the KoLPanel
	 * to update the panel's status.  It also provides a thread which is
	 * guaranteed to be a daemon thread for updating the frame which
	 * also retrieves a reference to the client's current settings.
	 */

	protected abstract class LabeledKoLPanel extends KoLPanel
	{
		private String panelTitle;
		private JPanel actionStatusPanel;
		private JLabel actionStatusLabel;

		public LabeledKoLPanel( String panelTitle, Dimension left, Dimension right )
		{	this( panelTitle, "apply", "defaults", left, right );
		}

		public LabeledKoLPanel( String panelTitle, String confirmButton, String cancelButton, Dimension left, Dimension right )
		{
			super( confirmButton, cancelButton, left, right, true );
			this.panelTitle = panelTitle;
		}

		protected void setContent( VerifiableElement [] elements )
		{	setContent( elements, true );
		}

		protected void setContent( VerifiableElement [] elements, boolean isLabelPreceeding )
		{	setContent( elements, isLabelPreceeding, false );
		}

		protected void setContent( VerifiableElement [] elements, boolean isLabelPreceeding, boolean bothDisabledOnClick )
		{
			super.setContent( elements, null, null, null, isLabelPreceeding, bothDisabledOnClick );

			if ( panelTitle != null )
				add( JComponentUtilities.createLabel( panelTitle, JLabel.CENTER, Color.black, Color.white ), BorderLayout.NORTH );

			actionStatusPanel = new JPanel();
			actionStatusPanel.setLayout( new GridLayout( 2, 1 ) );

			actionStatusLabel = new JLabel( " ", JLabel.CENTER );
			actionStatusPanel.add( actionStatusLabel );
			actionStatusPanel.add( new JLabel( " ", JLabel.CENTER ) );

			add( actionStatusPanel, BorderLayout.SOUTH );
		}

		public void setStatusMessage( int displayState, String s )
		{	actionStatusLabel.setText( s );
		}

		protected void actionCancelled()
		{
		}

		public void requestFocus()
		{
		}
	}

	/**
	 * An internal class which creates a panel which manages items.
	 * This is done because most of the item management displays
	 * are replicated.  Note that a lot of this code was borrowed
	 * directly from the ActionVerifyPanel class in the utilities
	 * package for Spellcast.
	 */

	protected abstract class ItemManagePanel extends ActionPanel
	{
		protected JList elementList;
		private VerifyButtonPanel buttonPanel;

		public ItemManagePanel( String title, String confirmedText, String cancelledText, LockableListModel elements )
		{
			elementList = new JList( elements );
			elementList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			elementList.setVisibleRowCount( 8 );

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout( new BorderLayout() );

			centerPanel.add( JComponentUtilities.createLabel( title, JLabel.CENTER,
				Color.black, Color.white ), BorderLayout.NORTH );
			centerPanel.add( new JScrollPane( elementList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );

			buttonPanel = new VerifyButtonPanel( confirmedText, cancelledText );

			JPanel actualPanel = new JPanel();
			actualPanel.setLayout( new BorderLayout( 20, 10 ) );
			actualPanel.add( centerPanel, BorderLayout.CENTER );
			actualPanel.add( buttonPanel, BorderLayout.EAST );

			setLayout( new CardLayout( 10, 10 ) );
			add( actualPanel, "" );
			buttonPanel.setBothDisabledOnClick( true );
		}

		protected abstract void actionConfirmed();
		protected abstract void actionCancelled();

		public void setEnabled( boolean isEnabled )
		{
			elementList.setEnabled( isEnabled );
			buttonPanel.setEnabled( isEnabled );
		}
	}

	/**
	 * An internal class which creates a panel which displays
	 * a generic scroll pane.  Note that the code for this
	 * frame was lifted from the ActionVerifyPanel found in
	 * the Spellcast package.
	 */

	protected abstract class LabeledScrollPanel extends ActionPanel
	{
		private JComponent scrollComponent;
		private VerifyButtonPanel buttonPanel;

		public LabeledScrollPanel( String title, String confirmedText, String cancelledText, JComponent scrollComponent )
		{
			this.scrollComponent = scrollComponent;

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout( new BorderLayout() );

			centerPanel.add( JComponentUtilities.createLabel( title, JLabel.CENTER,
				Color.black, Color.white ), BorderLayout.NORTH );
			centerPanel.add( new JScrollPane( scrollComponent, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );

			buttonPanel = new VerifyButtonPanel( confirmedText, cancelledText );
			buttonPanel.setBothDisabledOnClick( true );

			JPanel actualPanel = new JPanel();
			actualPanel.setLayout( new BorderLayout( 20, 10 ) );
			actualPanel.add( centerPanel, BorderLayout.CENTER );
			actualPanel.add( buttonPanel, BorderLayout.EAST );

			setLayout( new CardLayout( 10, 10 ) );
			add( actualPanel, "" );
		}

		public JComponent getScrollComponent()
		{	return scrollComponent;
		}

		protected abstract void actionConfirmed();
		protected abstract void actionCancelled();

		public void setEnabled( boolean isEnabled )
		{	buttonPanel.setEnabled( isEnabled );
		}
	}

	/**
	 * In order to do the things we need to do when windows are closed,
	 * this internal class is used to listen for window events.
	 */

	private class LocationAdapter extends WindowAdapter
	{
		public void windowClosing( WindowEvent e )
		{
			if ( client != null && client.getSettings().getProperty( "savePositions" ).equals( "true" ) )
			{
				Point p = getLocationOnScreen();
				client.getSettings().setProperty( frameName, ((int)p.getX()) + "," + ((int)p.getY()) );
				client.getSettings().saveSettings();
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing frames.
	 */

	protected class DisplayFrameMenuItem extends JMenuItem implements ActionListener
	{
		private Class frameClass;
		private CreateFrameRunnable displayer;

		public DisplayFrameMenuItem( String title, int mnemonic, Class frameClass )
		{
			super( title, mnemonic );
			addActionListener( this );

			this.frameClass = frameClass;

			Object [] parameters;
			if ( frameClass == LicenseDisplay.class )
			{
				parameters = new Object[3];
				parameters[0] = "KoLmafia: Copyright Notice";
				parameters[1] = LICENSE_FILENAME;
				parameters[2] = LICENSE_NAME;
			}
			else
			{
				parameters = new KoLmafia[1];
				parameters[0] = client;
			}

			this.displayer = new CreateFrameRunnable( frameClass, parameters );
		}

		public void actionPerformed( ActionEvent e )
		{
			displayer.setEnabled( isEnabled() );
			SwingUtilities.invokeLater( displayer );
		}
	}

	/**
	 * Internal class which opens the operating system's default
	 * browser to the given location.
	 */

	protected class DisplayPageMenuItem extends JMenuItem implements ActionListener
	{
		private String location;

		public DisplayPageMenuItem( String title, int mnemonic, String location )
		{
			super( title, mnemonic );
			addActionListener( this );

			this.location = location;
		}

		public void actionPerformed( ActionEvent e )
		{
			try
			{
				BrowserLauncher.openURL( location );
			}
			catch ( java.io.IOException e1 )
			{
				client.getLogStream().println( "Failed to open browser:" );
				client.getLogStream().print( e1 );
				e1.printStackTrace( client.getLogStream() );
			}
		}
	}

	/**
	 * Action listener responsible for handling links clicked
	 * inside of a <code>JEditorPane</code>.
	 */

	protected class KoLHyperlinkAdapter implements HyperlinkListener
	{
		public void hyperlinkUpdate( HyperlinkEvent e )
		{
			if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
			{
				String location = e.getDescription();

				if ( location.startsWith( "http://" ) || location.startsWith( "https://" ) )
				{
					// Attempt to open the URL on the system's default
					// browser.  This could theoretically cause problems,
					// but for now, let's just do a try-catch and cross
					// our fingers.

					try
					{
						BrowserLauncher.openURL( location );
					}
					catch ( java.io.IOException e1 )
					{
						client.getLogStream().println( "Failed to open browser:" );
						client.getLogStream().print( e1 );
						e1.printStackTrace( client.getLogStream() );
					}
				}
				else if ( location.indexOf( "submit()" ) == -1 )
				{
					// If it's a link internal to KoL, handle the
					// internal link.  Note that by default, this
					// method does nothing, but descending classes
					// can change this behavior.

					handleInternalLink( location );
				}
				else
				{
					// If it's an attempt to submit an adventure form,
					// examine the location string to see which form is
					// being submitted and submit it manually.

					String [] locationSplit = location.split( "\\." );
					String formID = locationSplit[ locationSplit.length - 2 ];

					String editorText = ((JEditorPane)e.getSource()).getText();

					String locationText = editorText.substring( editorText.substring( 0, editorText.indexOf( formID ) ).lastIndexOf( "<" ),
						editorText.indexOf( ">", editorText.lastIndexOf( formID ) ) );

					Matcher actionMatcher = Pattern.compile( "action=\"(.*?)\"" ).matcher( locationText );
					actionMatcher.find();

					Matcher inputMatcher = Pattern.compile( "<input.*?>" ).matcher( locationText );
					Pattern namePattern = Pattern.compile( "name=\"(.*?)\"" );
					Pattern valuePattern = Pattern.compile( "value=\"(.*?)\"" );

					String lastInput;
					int lastInputIndex = 0;
					Matcher nameMatcher, valueMatcher;
					StringBuffer inputString = new StringBuffer();

					while ( inputMatcher.find( lastInputIndex ) )
					{
						lastInputIndex = inputMatcher.end();
						lastInput = inputMatcher.group();

						nameMatcher = namePattern.matcher( lastInput );
						nameMatcher.find();

						valueMatcher = valuePattern.matcher( lastInput );
						valueMatcher.find();

						inputString.append( inputString.length() == 0 ? '?' : '&' );

						try
						{
							inputString.append( URLEncoder.encode( nameMatcher.group(1), "UTF-8" ) );
							inputString.append( '=' );
							inputString.append( URLEncoder.encode( valueMatcher.group(1), "UTF-8" ) );
						}
						catch ( Exception e2 )
						{
						}
					}

					// Now that the entire form string is known, handle
					// the appropriate internal link.

					handleInternalLink( actionMatcher.group(1) + inputString.toString() );
				}
			}
		}

		protected void handleInternalLink( String location )
		{
			if ( location.startsWith( "desc" ) || location.startsWith( "doc" ) || location.startsWith( "search" ) )
			{
				// Certain requests should open in a new window.
				// These include description data, documentation
				// and player searches.

				openRequestFrame( location );
			}
			else if ( KoLFrame.this instanceof RequestFrame )
			{
				// If this is a request frame, make sure that
				// you minimize the number of open windows by
				// making an attempt to refresh.

				((RequestFrame)KoLFrame.this).refresh( new KoLRequest( client, location, true ) );
			}
			else
			{
				// Otherwise, if this isn't a request frame,
				// open up a new request frame in order to
				// display the appropriate data.

				openRequestFrame( location );
			}
		}
	}

	/**
	 * An internal class which opens a new <code>RequestFrame</code>
	 * to the given frame whenever an action event is triggered.
	 */

	protected class MiniBrowserMenuItem extends JMenuItem implements ActionListener
	{
		private String location;

		public MiniBrowserMenuItem( String title, int mnemonic, String location )
		{
			super( title, mnemonic );
			addActionListener( this );

			this.location = location;
		}

		public void actionPerformed( ActionEvent e )
		{	openRequestFrame( location );
		}
	}

	private static final Class [] NOPARAMS = new Class[0];

	/**
	 * Internal class used to invoke the given no-parameter
	 * method on the given object.  This is used whenever
	 * there is the need to invoke a method and the creation
	 * of an additional class is unnecessary.
	 */

	protected class InvocationMenuItem extends JMenuItem implements ActionListener
	{
		private Object object;
		private Method method;

		public InvocationMenuItem( String title, int mnemonic, Object object, String methodName )
		{
			super( title, mnemonic );
			addActionListener( this );

			try
			{
				this.object = object;
				this.method = this.object == null ? null : this.object.getClass().getMethod( methodName, NOPARAMS );
			}
			catch ( Exception e )
			{
				System.out.println( object.getClass() );
				System.out.println( methodName );
			}
		}

		public void actionPerformed( ActionEvent e )
		{	(new InvocationThread()).start();
		}

		private class InvocationThread extends DaemonThread
		{
			public void run()
			{
				try
				{
					if ( method != null )
						method.invoke( object, null );
				}
				catch ( Exception e )
				{
				}
			}
		}
	}

	/**
	 * A method used to open a new <code>RequestFrame</code> which displays
	 * the given location, relative to the KoL home directory for the current
	 * session.  This should be called whenever <code>RequestFrame</code>s
	 * need to be created in order to keep code modular.
	 */

	public void openRequestFrame( String location )
	{
		Object [] parameters;

		if ( this instanceof RequestFrame )
		{
			parameters = new Object[4];
			parameters[0] = client;
			parameters[1] = this;
			parameters[2] = "Mini-Browser Window";
			parameters[3] = new KoLRequest( client, location );
		}
		else
		{
			parameters = new Object[3];
			parameters[0] = client;
			parameters[1] = "Mini-Browser Window";
			parameters[2] = new KoLRequest( client, location );
		}

		SwingUtilities.invokeLater( new CreateFrameRunnable( RequestFrame.class, parameters ) );
	}

	/**
	 * An internal class used to handle requests to open a new frame
	 * using a local panel inside of the adventure frame.
	 */

	protected class KoLPanelFrameMenuItem extends JMenuItem implements ActionListener
	{
		private CreateFrameRunnable creator;

		public KoLPanelFrameMenuItem( String title, int mnemonic, KoLPanel panel )
		{
			super( title, mnemonic );
			addActionListener( this );

			Object [] parameters = new Object[3];
			parameters[0] = client;
			parameters[1] = title;
			parameters[2] = panel;

			creator = new CreateFrameRunnable( KoLPanelFrame.class, parameters );
		}

		public void actionPerformed( ActionEvent e )
		{	creator.run();
		}
	}

	/**
	 * Utility method which retrieves an integer value from the given
	 * field.  In the event that the field does not contain an integer
	 * value, the number "0" is returned instead.
	 */

	protected static final int getValue( JTextField field )
	{	return getValue( field, 0 );
	}

	/**
	 * Utility method which retrieves an integer value from the given
	 * field.  In the event that the field does not contain an integer
	 * value, the default value provided will be returned instead.
	 */

	protected static final int getValue( JTextField field, int defaultValue )
	{
		try
		{
			return field.getText() == null || field.getText().length() == 0 ?
				defaultValue : df.parse( field.getText().trim() ).intValue();
		}
		catch ( Exception e )
		{
			// If something's wrong with the parsing, assume
			// that the person wanted the default value.

			return defaultValue;
		}
	}
}