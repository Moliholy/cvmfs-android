package ch.cern.cvmfs.model;


public class RepositoryDescription {

    private String name;
    private String fqrn;
    private String url;

    public RepositoryDescription(String name, String fqrn, String url) {
        this.name = name;
        this.fqrn = fqrn;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFqrn() {
        return fqrn;
    }

    public void setFqrn(String fqrn) {
        this.fqrn = fqrn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

	public String toStoredFormat() {
		return name + ";" +
				fqrn.replace(" ", "") + ";" +
				url.replace(" ", "");
	}

    @Override
    public String toString() {
        return name + " (" + fqrn + ")";
    }
}
