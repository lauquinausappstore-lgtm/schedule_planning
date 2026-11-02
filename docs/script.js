const search = document.querySelector('#search');
const links = [...document.querySelectorAll('#navigation a')];
const sections = [...document.querySelectorAll('main > section')];
search.addEventListener('input', () => {
  const query = search.value.trim().toLocaleLowerCase();
  let matches = 0;
  links.forEach(link => {
    const section = document.querySelector(link.getAttribute('href'));
    const found = !query || `${section.textContent} ${section.dataset.keywords}`.toLocaleLowerCase().includes(query);
    link.hidden = !found;
    if (found) matches++;
  });
  document.querySelector('#no-results').hidden = matches > 0;
});
if ('IntersectionObserver' in window) {
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      links.forEach(link => {
        if (link.hash === `#${entry.target.id}`) link.setAttribute('aria-current', 'location');
        else link.removeAttribute('aria-current');
      });
    });
  }, { rootMargin: '-15% 0px -60% 0px' });
  sections.forEach(section => observer.observe(section));
}
