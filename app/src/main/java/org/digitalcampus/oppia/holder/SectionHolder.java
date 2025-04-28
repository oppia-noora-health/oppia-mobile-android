package org.digitalcampus.oppia.holder;

import org.digitalcampus.oppia.model.Section;

public class SectionHolder {

    private static Section section;

    public static void setSection(Section sec) {
        section = sec;
    }

    public static Section getSection() {
        return section;
    }

    public static void clear() {
        section = null;
    }
}
