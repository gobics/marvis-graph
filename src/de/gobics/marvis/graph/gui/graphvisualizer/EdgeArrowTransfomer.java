/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.graph.gui.graphvisualizer;

import de.gobics.marvis.graph.*;
import edu.uci.ics.jung.graph.util.Context;
import org.apache.commons.collections15.*;

/**
 *
 * @author manuel
 */
class EdgeArrowTransfomer implements Predicate<Context<edu.uci.ics.jung.graph.Graph<GraphObject, Relation>, Relation>> {

	@Override
	public boolean evaluate(Context<edu.uci.ics.jung.graph.Graph<GraphObject, Relation>, Relation> t) {
		if( t.element.getType().equals(RelationshipType.REACTION_HAS_PRODUCT)
				|| t.element.getType().equals(RelationshipType.REACTION_HAS_SUBSTRATE) )
			return true;
		return false;
	}
}
