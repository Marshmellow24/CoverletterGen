import spacy
from . import pdf_extraction as pdf


def noun_extract(file_path):
    """
    This method extracts nouns and proposition nouns from a text or pdf file

    :param file_path: path of text file or pdf
    :return: string of pos tagged words extracted from the text
    """

    nlp = spacy.load('de_core_news_lg')
    output = []
    if file_path.endswith(".pdf"):
        data_file = pdf.parse_pymupdf(file_path, True)
    else:
        data_file = open(file_path, "r", encoding="utf-8")
        data_file = data_file.readlines()
    for line in data_file:
        doc = nlp(line)
        for token in doc:
            if token.pos_ in ["NOUN", "PROPN"]:
                if token.text:
                    output.append(token.text)
    # remove duplicates by creating dict (dict innately only contains unicates) from list and casting back as list
    output = list(dict.fromkeys(output))
    # transform list to string so spacy nlp can parse it
    output_str = " ".join(output)
    return output_str


# file= r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\output\cv_68.txt"
#
# print(noun_extract(file))

# file = r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\cv_23.pdf"

# print(noun_extract(file))


