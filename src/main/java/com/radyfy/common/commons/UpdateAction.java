package com.radyfy.common.commons;

import org.bson.Document;

@FunctionalInterface
public interface UpdateAction {

    void run(Document t);
}
