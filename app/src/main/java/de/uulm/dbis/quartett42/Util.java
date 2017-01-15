package de.uulm.dbis.quartett42;

import java.util.ArrayList;
import java.util.HashMap;

import de.uulm.dbis.quartett42.data.Card;
import de.uulm.dbis.quartett42.data.ImageCard;
import de.uulm.dbis.quartett42.data.Property;

/**
 * Utilities class (only static methods)
 *
 * Created by Luis on 12.01.2017.
 */
public class Util {

    /**
     * for the list view we need to create an arraylist of strings containing
     * - name
     * - maxwinner
     * - value
     * - unit
     * @param card the currently selected card
     * @return the list of attributes formatted for display
     */
    public static ArrayList<Property> buildAttrList(ArrayList<Property> propList, Card card) {
        ArrayList<Property> attrList = new ArrayList<Property>();

        // loop through each property
        for (Property p: propList) {
            // get the cards value
            double attrValue = card.getAttributeMap().get(p.getName());
            // put it inside the property for the adapter
            Property cardAttr = new Property(p.getName(), p.getUnit(), p.isMaxWinner(), attrValue);
            attrList.add(cardAttr);
        }

        return attrList;
    }

    public static Card buildCardFromRaw(
            ArrayList<String> imgUriList,
            ArrayList<String> imgDescList,
            String attrName,
            double attrValue,
            String cardName) {

        // get the data into the right format
        ArrayList<ImageCard> imageCards = new ArrayList<>();
        for (int i = 0; i < imgUriList.size(); i++) {
            imageCards.add(new ImageCard(imgUriList.get(i), imgDescList.get(i)));
        }

        HashMap<String, Double> attributeMap = new HashMap<>();
        attributeMap.put(attrName, attrValue);
        return new Card(
                cardName,
                0, // id does not matter here
                imageCards,
                attributeMap
        );

    }
}