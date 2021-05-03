import csv
import json
import spacy
import time
import sys
import os
from Data_Parsing import pdf_extraction as pdf
import argparse
 

def create_dict_from_cv(file_path):
    """
    This method uses the previously trained NER-model and recognizes the relevant cv entitites in a pdf or txt file.

    :param file_path: path of txt/pdf file
    :return:
    """
    nlp = spacy.load("cv_ents")
    doc = nlp(pdf.parse_pymupdf(file_path, multi_line=False))

    experiences = [ent.text for ent in doc.ents if ent.label_ == "Experience"]
    skills = [ent.text for ent in doc.ents if ent.label_ == "Skills"]
    languages = [ent.text for ent in doc.ents if ent.label_ == "Languages"]
    current_job = [ent.text for ent in doc.ents if ent.label_ == "CurrentJob"]
    degrees = [ent.text for ent in doc.ents if ent.label_ == "Degrees"]
    branch = [ent.text for ent in doc.ents if ent.label_ == "Branch"]
    titles = [ent.text for ent in doc.ents if ent.label_ == "Titles"]
    charity = [ent.text for ent in doc.ents if ent.label_ == "Charity"]
    drivers = [ent.text for ent in doc.ents if ent.label_ == "DriversLicense"]

    if charity:
        charity = "true"
    else:
        charity = "false"

    if drivers:
        drivers = "true"
    else:
        drivers = "false"

    cv_output = {"Current Job": current_job, "Experience": experiences, "Branch": branch, "Skills": skills, "Languages": languages,
                 "Titles": titles, "Degrees": degrees, "Charity": charity, "Drivers License": drivers}

    return cv_output


def generate_json(cv_dict):
    """
    This method generates a json file from the structured cv data and saves it to /data/buffer/
    The file name can be identified by a timestamp

    :param cv_dict: the structured cv data containing the entities and values
    :return: returns the file name as a string
    """
    dir_path = os.path.dirname(__file__)
    output_path = os.path.join(dir_path, "../../data/buffer/")

    data = {"content": cv_dict}
    timestr = time.strftime("%Y%m%d_%H%M%S")

    with open(output_path + r"cv_parsed" + timestr + r".json", "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

    return r"cv_parsed" + timestr + r".json"


def append_dict_to_csv(csv_file, cv_dict):
    """
    This method takes a dictionary, formats the containing lists of strings into single strings and writes them into the casebase csv file.
    :param csv_file: casebase csv file
    :param cv_dict: dict containing entities from cv
    :return:
    """

    # read header from existing csv casebase and use this to put similarities into order
    with open(csv_file, "r", encoding="utf-8") as file:
        csv_reader = csv.reader(file, delimiter=";")
        header = [row for row in csv_reader][0]  # read only first row of csv file
    try:
        # format the values so that the are one string and can be written as such into the csv file (else they are put out like this: [x,y,z])
        for k, v in cv_dict.items():
            cv_dict[k] = ",".join(v)
        with open(csv_file, 'a', encoding="utf-8") as csvfile:
            writer = csv.DictWriter(csvfile, fieldnames=header, delimiter=";")
            # writer.writeheader()  # no need to write the header as it already is contained in the file
            writer.writerow(cv_dict)
            print("CV parsed")
    except IOError:
        print("I/O error")


# def parse(path):
#    print(generate_json(create_dict_from_cv(path)))



# parser = argparse.ArgumentParser(description='Process some integers.')
# parser.add_argument('path', metavar='N', type=str, nargs="+", help='an integer for the accumulator')
# parser.add_argument('--sum', dest='accumulate', action='store_const', const=sum, help='sum the integers (default: find the max)')
#
# args = parser.parse_args()
# print(sys.argv[1])

# argument which is passed from parse.bat
path = sys.argv[1]
# return to java method parse_cv()
print(generate_json(create_dict_from_cv(path)))


