package com.radyfy.common.model.crm.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import com.radyfy.common.model.BaseEntityModel;
import com.radyfy.common.model.dao.MemoryCached;

import java.util.List;

@Getter
@Setter
@ToString
@MemoryCached
@Document(collection = "app_menu")
public class AppMenu extends BaseEntityModel {
    private List<MenuItem> menuItems;
    private org.bson.Document iconSetting;
    private List<MenuItem> bottomMenu;
}
