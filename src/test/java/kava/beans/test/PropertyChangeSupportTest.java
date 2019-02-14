package kava.beans.test;

import org.junit.Assert;
import org.junit.Test;
import kava.beans.PropertyChangeSupport;

/**
 * @author VISTALL
 * @since 2019-02-14
 */
public class PropertyChangeSupportTest extends Assert
{
	private static final Object source = new String("source");

	private static final String someField = "someField";
	private static final String someOtherField = "someOtherField";

	private static final String someValue = "someValue";
	private static final String someValueNew = "someValueNew";

	@Test
	public void testFireListenerWithName()
	{
		PropertyChangeSupport support = new PropertyChangeSupport(source);

		String[] enterPropertyName = new String[1];
		Object[] enterPropertyOldValue = new Object[1];
		Object[] enterPropertyNewValue = new Object[1];

		support.addPropertyChangeListener(someField, event -> {
			enterPropertyName[0] = event.getPropertyName();
			enterPropertyOldValue[0] = event.getOldValue();
			enterPropertyNewValue[0] = event.getNewValue();
		});

		support.firePropertyChange(someField, null, someValue);

		assertEquals(enterPropertyName[0], someField);
		assertEquals(enterPropertyOldValue[0], null);
		assertEquals(enterPropertyNewValue[0], someValue);

		support.firePropertyChange(someField, someValue, someValueNew);

		assertEquals(enterPropertyName[0], someField);
		assertEquals(enterPropertyOldValue[0], someValue);
		assertEquals(enterPropertyNewValue[0], someValueNew);
	}
}
