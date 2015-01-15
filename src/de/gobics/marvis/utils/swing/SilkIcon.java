package de.gobics.marvis.utils.swing;

import java.net.URL;
import javax.swing.ImageIcon;

/**
 * An implementation of the {@link ImageIcon} to use the SILK icon set.
 *
 * The Silk icons copyrighted by <a
 * href="http://www.famfamfam.com/lab/icons/silk">Mark James</a> licensed under
 * the <a href="http://creativecommons.org/licenses/by/2.5/">Creative Commons
 * Attribution 2.5 License</a>.
 *
 * @author manuel
 */
public class SilkIcon extends ImageIcon {

	private SilkIcon(URL url) {
		super(url);
	}

	public static SilkIcon getIcon(String name) {
		URL url = SilkIcon.class.getResource("icons/silk/" + name + ".png");
		if (url == null) {
			return null;
		}
		return new SilkIcon(url);
	}

	public static SilkIcon getCut() {
		return getIcon("cut");
	}

	public static SilkIcon getPaste() {
		return getIcon("page_paste");
	}

	public static SilkIcon getCopy() {
		return getIcon("page_copy");
	}

	public static SilkIcon getDelete() {
		return getIcon("delete");
	}

	public static SilkIcon getRename() {
		return getIcon("textfield_rename");
	}

	public static SilkIcon getLoad() {
		return getIcon("pencil");
	}

	public static SilkIcon getSave() {
		return getIcon("disk");
	}

	public static SilkIcon getCancel() {
		return getIcon("cancel");
	}

	public static SilkIcon getAccept() {
		return getIcon("accept");
	}
	
	public static SilkIcon getExit(){
		return getIcon("door_out");
	}
	
	public static SilkIcon getNew(){
		return getIcon("page");
	}
}
