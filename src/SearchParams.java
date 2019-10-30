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
    public boolean isCaseSensitive;
    public boolean isFileSizeSkip;
    public long isFileSizeSkip_size;//In bytes
    public boolean isRegex;


    public int maxMatchesFiles = 100; //TODO: Not used

    public SearchParams(String searchString, String path) {
        this.searchString = searchString;
        this.path = path;
    }
}
