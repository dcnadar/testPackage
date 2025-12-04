package com.radyfy.common.model.dynamic.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.radyfy.common.model.dynamic.table.Button;
import com.radyfy.common.response.Label;
import com.radyfy.common.utils.BsonDocumentUtils;
import com.radyfy.common.utils.Utils;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CardData implements Serializable {

    private String title;
    private String thumb;
    private String thumbInitials;
    private String thumbColor;
    private List<String> subtitles;
    private String type;
    private Button crossButton;
    private String slug;
    private Label label;
    private List<Button> actions;

    public CardData generate(Document obj) {
        final CardData cardData = new CardData();

        if (Utils.isNotEmpty(title)) {
            // handle for multiple keys in title by space separated and append in cardData.title
            String[] titleKeys = title.split(" ");
            for (String titleKey : titleKeys) {
                String value = BsonDocumentUtils.getDataValueAsString(obj, titleKey);
                if(Utils.isNotEmpty(value)){
                    if (Utils.isNotEmpty(cardData.getTitle())) {
                        cardData.setTitle(cardData.getTitle() + " " + value);
                    } else {
                        cardData.setTitle(value);
                    }
                }
            }
        }

        if(Utils.isNotEmpty(type)){
            // handle for multiple keys in type by space separated and append in cardData.type
            String[] typeKeys = type.split(" ");
            for (String typeKey : typeKeys) {
                String value = BsonDocumentUtils.getDataValueAsString(obj, typeKey);
                if(Utils.isNotEmpty(value)){
                    if (Utils.isNotEmpty(cardData.getType())) {
                        cardData.setType(cardData.getType() + " " + value);
                    } else {
                        cardData.setType(value);
                    }
                }
            }
        }

        if (Utils.isNotEmpty(thumb)) {
            Object thumbValue = BsonDocumentUtils.getDataValue(obj, thumb);
            if(thumbValue != null){
                if (thumbValue instanceof List) {
                    cardData.setThumb(((List<String>) thumbValue).get(0));
                } else {
                    cardData.setThumb(thumbValue.toString());
                }
            }
        }

        if (Utils.isNotEmpty(subtitles)) {
            List<String> st = new ArrayList<>();
            subtitles.forEach(f -> {
                Object value = BsonDocumentUtils.getDataValueAsString(obj, f);
                if (value instanceof List) {
                    st.addAll((List<String>) value);
                } else {
                    st.add((String) value);
                }
            });
            cardData.setSubtitles(st);
        }

        if (Utils.isNotEmpty(slug)) {
            cardData.setSlug(slug);
        }
        return cardData;
    }

    @JsonIgnore
    public List<String> getFetchableFields() {
        List<String> fields = new ArrayList<>();

        if (Utils.isNotEmpty(title)) {
            String[] titleKeys = title.split(" ");
            for (String titleKey : titleKeys) {
                fields.add(titleKey);
            }
        }

        if (Utils.isNotEmpty(type)) {
            String[] typeKeys = type.split(" ");
            for (String typeKey : typeKeys) {
                fields.add(typeKey);
            }
        }

        if (Utils.isNotEmpty(thumb)) {
            fields.add(thumb);
        }

        if (Utils.isNotEmpty(subtitles)) {
            fields.addAll(subtitles);
        }

        return fields;
    }

}
