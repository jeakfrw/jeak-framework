package de.fearnixx.jeak.util;

import java.util.regex.Pattern;

/**
 * Comparator class that allows comparison of SemVer-compliant version numbers.
 *
 * @author Magnus Leßmann
 * @see #compare(String, String) for details.
 * @since 1.2.0
 */
public abstract class SemVerComparator {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+).*");
    private static final String ERR_UNSUPPORTED_STR = "Unsupported SemVer version string: ";
    private static final int POS_MAJOR = 0;
    private static final int POS_MINOR = 1;
    private static final int POS_BUGFIX = 2;
    private static final int OFFSET = 3;

    private SemVerComparator() {
    }

    /**
     * Compares two SemVer-numbers in the form of {@code <major>.<minor>.<bugfix><suffix>} (Where major, minor and bugfix may only be integers).
     * Returns {@code true} when the following statements are <em>all true</em>:
     * <ul>
     *     <li>Both major versions are the same.</li>
     *     <li>Both minor versions are the same OR the minor version of the target is higher than the dependent minor version.</li>
     *     <li>If the minor versions are equal: (Both bugfix versions are the same or the bugfix version of the target is higher than the dependent bugfix version.)</li>
     * </ul>
     * Otherwise, {@code false} is returned.
     * Both suffixes are ignored.
     *
     * @param dependent The version string of the unit dependent on the target unit.
     * @param target    The version string of the unit that is being depended on.
     * @throws IllegalArgumentException when any provided version string does not match the provided pattern.
     * @apiNote In case it wasn't clear enough: <em>The order of arguments is important for the result!</em>
     */
    @SuppressWarnings("RedundantIfStatement")
    public static boolean compare(String dependent, String target) {
        final var depMatcher = VERSION_PATTERN.matcher(dependent);
        final var tarMatcher = VERSION_PATTERN.matcher(target);

        if (!depMatcher.matches()) {
            throw new IllegalArgumentException("Unsupported SemVer version string: " + dependent);
        } else if (!tarMatcher.matches()) {
            throw new IllegalArgumentException(ERR_UNSUPPORTED_STR + dependent);
        }

        final var nums = new int[]{
                Integer.parseInt(depMatcher.group(1)),
                Integer.parseInt(depMatcher.group(2)),
                Integer.parseInt(depMatcher.group(3)),
                Integer.parseInt(tarMatcher.group(1)),
                Integer.parseInt(tarMatcher.group(2)),
                Integer.parseInt(tarMatcher.group(3))
        };

        if (nums[POS_MAJOR] != nums[POS_MAJOR + OFFSET]) {
            return false;

        } else if (nums[POS_MINOR] > nums[POS_MINOR + OFFSET]) {
            return false;

        } else if (nums[POS_BUGFIX] > nums[POS_BUGFIX + OFFSET]) {
            return false;
        }
        return true;
    }
}
