package com.bestinslot.client.query;

import java.util.List;

public interface ItemQueryProvider {
    List<CategorizedItem> query(ItemCategory category, SortCriteria sort, boolean ascending);
}
