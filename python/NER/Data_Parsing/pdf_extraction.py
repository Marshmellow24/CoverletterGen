import re
import fitz
from tika import parser


def parse_pdf(file_path, write=True):
    """
    Extracts text from pdf files while using tika parser

    :param file_path: file to be parsed
    :param write: write output to hard disk
    :return: list of strings
    """
    rawText = parser.from_file(file_path)
    rawList = rawText["content"].splitlines()

    rawTextClean = re.compile(r"["
                              r"\t"
                              # r"\d"
                              r"]+")

    remove_special_chars = re.compile(r"^\W ")
    remove_punc = re.compile(r"[\.,/:()\[\]\\%!?_] ?| ?[\.,/:()\[\]\\%!?_]|^–")
    remove_multiple_ws = re.compile(r" +")

    cleanList = [x for x in rawList if x]
    print(cleanList)
    cleanList = [re.sub(rawTextClean, " ", x).strip() for x in cleanList]
    cleanList = [re.sub(remove_special_chars, "", x).strip() for x in cleanList if x]
    print(cleanList)
    # cleanList = [re.sub(remove_punc, " ", x).strip() for x in cleanList if x]
    # print(*cleanList, sep="\n")
    print(cleanList)
    cleanList = [re.sub(remove_multiple_ws, " ", x).strip() for x in cleanList if x]
    print(*cleanList, sep="\n")

    if write:
        file_out = file_path.replace("bewerbungen_raw\\", "bewerbungen_raw\\output\\")
        out_file = open(file_out.replace(".pdf", ".txt"), "w", encoding="utf-8")
        for line in cleanList:
            out_file.write(line + "\n")

    return cleanList


def parse_pymupdf(file_path, multi_line=True):
    """
    extracts text from pdf file while using pymupdf (fitz) and returns a (single line) string

    :param multi_line: boolean for deciding if output is single line string or list of strings
    :param file_path: file to be parsed
    :return: text as single line string or list of strings
    """
    doc = fitz.open(file_path)
    text = ""
    for page in doc:
        text = text + str(page.getText())
    if multi_line:
        output = text.split("\n")
    else:
        output = " ".join(text.split("\n"))
        output = re.sub(r" +", " ", output).strip()

    return output



