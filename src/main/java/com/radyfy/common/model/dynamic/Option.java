package com.radyfy.common.model.dynamic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.radyfy.common.model.dynamic.card.CardData;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Option implements Serializable {
    private String key;
    private Object value;
    private String thumb;
    private String thumbInitials;
    private String thumbColor;
    private String description;
    private Map<String, Object> meta;

    public Option(){
        super();
    }

    public Option(String key, Object value){
        super();
        this.key = key;
        this.value = value;
    }

    public Option(String key, Object value, String description){
        this(key, value);
        this.description = description;
    }

    public CardData generateCardData(){
        CardData cardData = new CardData();
        if(this.getValue() != null){
            cardData.setTitle((String) this.getValue());
        }
        if(this.getThumb() != null){
            cardData.setThumb(this.getThumb());
        }
        if(this.getThumbInitials() != null){
            cardData.setThumbInitials(this.getThumbInitials());
        }
        if(this.getThumbColor() != null){
            cardData.setThumbColor(this.getThumbColor());
        }
        if(this.getDescription() != null){
            cardData.setSubtitles(Collections.singletonList(this.getDescription()));
        }
        return cardData;
    }
}
