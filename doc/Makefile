
IMAGES= $(shell find images/ -iname '*.svg' | sed 's/\.svg/\.pdf/');


images/%.pdf: images/%.svg
	inkscape --export-area-drawing --export-dpi=900 --export-pdf=$@ $<


%.pdf: %.tex $(IMAGES)
	pdflatex --halt-on-error $< \
		&& bibtex $(shell echo $@ | sed 's/\.pdf/\.aux/') \
		&& pdflatex $< \
		&& pdflatex $<


all: marvis_graph.pdf
