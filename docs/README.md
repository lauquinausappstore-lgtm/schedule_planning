# Documentation website

Static documentation for the updated Dayline Android app. No dependencies or build step.

Serve the Dayline project root with a static web server, then visit `/docs/`. For example:

```sh
python3 -m http.server 8080
```

Open `http://localhost:8080/docs/`. For GitHub Pages, configure deployment from the branch's `/docs` folder after these files have been pushed. A repository Pages URL will usually have a repository-name prefix; select root deployment if the host must expose exactly `/docs/`.

Files: `index.html`, `styles.css`, and `script.js`. Includes mobile layout, keyboard navigation, search across documentation topics, and expandable troubleshooting answers. Search filters the sidebar topic links; it does not hide article sections.

The website has not been deployed or pushed to GitHub. HTML links and asset paths were checked locally; browser visual verification was not available.
