/**
 * Encapsulation class for a search query that contains data about the search query.
 */
public class SearchParams {

    public String searchString;
    public String path;

    //public boolean isRegex; //Not yet implimented
    public boolean isRecursive;
    public boolean isFollowSymbolicLinks;
    public boolean isIncludeFilename;
    public int maxMatchesFiles = 100;
    public boolean isCaseSensitive;
    public SearchParams(String searchString, String path) {
        this.searchString = searchString;
        this.path = path;
    }
}
