package com.radyfy.common.model.crm.grid.table;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.radyfy.common.utils.Utils;

@Getter
@Setter
public class GridParam implements Serializable {

    public enum ServerValue{
        current_user_id,
        fixed_value,
        filter_value;
    }

    private String key;
    private boolean required;

    // this key is actual key in the mongo document, if this is not available then 'key' will be used as actual for query
    private String documentKey;
    private ServerValue serverValue;
    private String value;
    public GridParam(){
        super();
    }

    public String getKey() {
        return key;
    }

    public GridParam(String key){
        super();
        this.key = key;
        this.required = false;
    }

    public GridParam(String key, boolean required){
        super();
        this.key = key;
        this.required = required;
    }

    public static List<GridParam> getByKeys(Set<String> keys){
        return keys.stream().map(GridParam::new).collect(Collectors.toList());
    }

    public String getFilterKey(){
        if(Utils.isNotEmpty(documentKey)){
            return documentKey;
        }
        return key;
    }
}
