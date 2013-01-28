/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.gui.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author manuel
 */
public class PermutationTestTest {

	private PermutationTest instance;

	public PermutationTestTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		instance = new PermutationTest(null, null, null, null);
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of hasHigherEqual method, of class PermutationTest.
	 */
	@Test
	public void testHasHigherEqual() {
		System.out.println("hasHigherEqual");
		Collection<Comparable> scores = new ArrayList<>(5);
		scores.addAll(Arrays.asList(new Integer[]{1, 2, 3, 4, 5}));
		assertEquals(false, instance.hasHigherEqual(scores, 10));
		assertEquals(false, instance.hasHigherEqual(scores, 6));
		assertEquals(true, instance.hasHigherEqual(scores, 5));
		assertEquals(true, instance.hasHigherEqual(scores, 4));
		assertEquals(true, instance.hasHigherEqual(scores, 0));
		assertEquals(true, instance.hasHigherEqual(scores, -1));
	}

	/**
	 * Test of countFamilyWiseErrors method, of class PermutationTest.
	 */
	@Test
	public void testCountFamilyWiseErrors() {
		System.out.println("countFamilyWiseErrors");
		LinkedList<Collection<Comparable>> scores = new LinkedList<>();
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{1, 2, 3, 4, 5})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{2, 3, 4, 5, 6})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{3, 4, 5, 6, 7})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{4, 5, 6, 7, 8})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{5, 6, 7, 8, 9})));

		assertEquals(0, instance.countFamilyWiseErrors(scores, 100));
		assertEquals(0, instance.countFamilyWiseErrors(scores, 10));
		assertEquals(1, instance.countFamilyWiseErrors(scores, 9));
		assertEquals(2, instance.countFamilyWiseErrors(scores, 8));
	}

	/**
	 * Test of countFalseDiscoveryErrors method, of class PermutationTest.
	 */
	@Test
	public void testCountFalseDiscoveryErrors() {
		System.out.println("countFalseDiscoveryErrors");

		LinkedList<Collection<Comparable>> scores = new LinkedList<>();
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{1, 2, 3, 4, 5})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{2, 3, 4, 5, 6})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{3, 4, 5, 6, 7})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{4, 5, 6, 7, 8})));
		scores.add(new LinkedList<Comparable>(Arrays.asList(new Integer[]{5, 6, 7, 8, 9})));

		assertEquals(0, instance.countFalseDiscoveryErrors(scores, 100));
		assertEquals(0, instance.countFalseDiscoveryErrors(scores, 10));
		assertEquals(1, instance.countFalseDiscoveryErrors(scores, 9));
		assertEquals(3, instance.countFalseDiscoveryErrors(scores, 8));



	}
}
