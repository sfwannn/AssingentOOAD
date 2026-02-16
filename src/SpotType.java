public enum SpotType {
    COMPACT, REGULAR, HANDICAPPED, RESERVED;

    public String displayName() {
        switch (this) {
            case COMPACT:
                return "Compact";
            case REGULAR:
                return "Regular";
            case HANDICAPPED:
                return "Handicapped";
            case RESERVED:
                return "Reserved";
            default:
                return name();
        }
    }

    public static SpotType fromDisplayName(String name) {
        if (name == null) {
            return null;
        }

        switch (name) {
            case "Compact":
                return COMPACT;
            case "Regular":
                return REGULAR;
            case "Handicapped":
                return HANDICAPPED;
            case "Reserved":
                return RESERVED;
            default:
                return null;
        }
    }
}