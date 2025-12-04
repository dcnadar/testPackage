package com.radyfy.common.commons;

import java.util.*;

import com.radyfy.common.model.dynamic.Option;
import com.radyfy.common.response.CheckboxGroup;

public class Permissions {

    public static final List<CheckboxGroup> permissions = new ArrayList<>();
    static {
        permissions.add(new CheckboxGroup("Staff", Arrays.asList(
                new Option("staff.list", "Staff List"),
                new Option("staff.create", "Staff Create"),
                new Option("staff.update", "Staff Update"),
                new Option("staff.transfer", "Staff Transfer"),
                new Option("staff.transfer.history", "Staff Transfer History")
        )));
        permissions.add(new CheckboxGroup("Admission", Arrays.asList(
                new Option("admission.list", "Enquiry List"),
                new Option("admission.create", "Enquiry Create"),
                new Option("admission.update", "Enquiry Update")
        )));
        permissions.add(new CheckboxGroup("Setting", Arrays.asList(
                new Option("setting.department", "Setting Department"),
                new Option("setting.branch", "Setting Branch"),
                new Option("setting.role", "Setting Role")
        )));
    }
}
