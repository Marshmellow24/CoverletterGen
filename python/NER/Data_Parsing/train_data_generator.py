import re
import clipboard
import xlrd as xl


def extract_information(file_path, start, end, ent="", append_ents=False):
    """
    This method takes a text file and extracts a slice of it and returns a list of strings. entities can be appended.

    :param file_path: text file (e.g. parsed pdf file)
    :param start: start line from which shall be sliced
    :param end: end line to slice to
    :param ent: which entity Type should be annotated
    :param append_ents: enable/disable entity annotations appendings
    :return: list of strings
    """
    entts = {"entities": [(0, 0, ent)]}  # just a template, entity indices have to be set manually still
    with open(file_path, "r", encoding="utf-8") as doc:
        # only append entity annotations if append_ents parameter is True
        if not append_ents:
            data = [line.rstrip() for line in doc.readlines()[start:end]]
        else:
            data = [(line.rstrip(), entts) for line in doc.readlines()[start:end]]
    return data


def merge_list_strings(*cols, ent="", ent_col=0):
    """
    This method wants to merge extracted columns (from pdf) back to their originally intented strings/line format. Also
    finds entity index range (only viable if one column solely contains entity) and appends annotation dict to list item.

    :param cols: arbitrary number of list of strings (columns in pdf)
    :param ent: entity abbreviation which shall be annotated
    :param ent_col: column that contains entities
    :return: list of merged column lines
    """
    # merge lists so they are structured like this [a,b,c][x,y,z][1,2,3] -> [(a,x,1),(b,y,2),(c,z,3)]
    merged_list = list(zip(*cols))

    # merge tuples in list so they are structured like this [(a,x,1),(b,y,2),(c,z,3)] -> [(a x 1),(b y 2),(c z 3)]
    for t in merged_list:
        out = [" ".join(t) for t in merged_list]

    # find index of item in ent_col, which ideally contains entity. Give out index range and append dict with correct
    # index range. [(a x 1),(b y 2),(c z 3)] -> [(a x 1, {ents: (ent_col_start, ent_col_end, SKL)}),
    # (b y 2, {ents: (ent_col_start, ent_col_end, SKL)}),(c z 3, {ents: (ent_col_start, ent_col_end, SKL)})]
    i = 0
    final = []
    for item in out:
        index_range = (item.find(cols[ent_col][i]), len(cols[ent_col][i]) + item.find(cols[ent_col][i]), ent)
        entts = ({"entities": [index_range]})
        # list comprehension does not work properly, would only append last entts value
        final.append((item, entts))
        i += 1

    # only return appended list if ent tag is in arguments
    if ent:
        return final
    else:
        return out


def extract_index_pos(file_path, search_keyword, cv_index, annot=""):
    """
    This method makes manual entity annotation of parsed data easier. Takes text file as input, which should contain one raw data item per line.
    Line specified by argument will be parsed. Copy paste desired token from text and insert as argument. This method returns the indices of the token
    in a tuple form so that they can be copy-pasted into the train-data txt file. Also prints out the token to make sure the correct substring is
    matched.
    :param file_path: path of train data txt file (requires one tuple per line max)
    :param search_keyword: the token which shall be matched and whose index range shall be returned
    :param cv_index: the line of the document
    :param annot: Optional parameter for when you dont want to type in entity tag everytime by hand
    :return: a tuple with an entity tag
    """
    with open(file_path, "r", encoding="utf-8") as f:
        sentence = f.readlines()[cv_index]

    pat = re.compile(re.escape(search_keyword))

    print(pat.search(sentence))
    # Entity type shortcuts
    if not annot:
        key = input("Ent_Tag:")
        if key == "1":
            ent = "Skills"
        elif key == "2":
            ent = "Experience"
        elif key == "3":
            ent = "CurrentJob"
        elif key == "4":
            ent = "Languages"
        elif key == "5":
            ent = "Degrees"
        elif key == "6":
            ent = "Name"
        else:
            ent = key
    else:
        ent = annot
    print("(" + str(pat.search(sentence).span()[0]) + ", " + str(pat.search(sentence).span()[1]) + ", \"" + ent + "\"), ")
    clipboard.copy("(" + str(pat.search(sentence).span()[0]) + ", " + str(pat.search(sentence).span()[1]) + ", \"" + ent + "\"), ")
    print("Above is copied to clipboard")
    print("\n")
    print(sentence[pat.search(sentence).span()[0]:pat.search(sentence).span()[1]])


def extract_from_xls(file_path, sheet_index=0):
    """
    This method takes a xls file in this format: text, string, start_index, end_index, entity_type and converts it to an entities list containing
    text sample with their according entity annotations. These lists can be used to train spacy NER models.
    :param file_path: path of
    :param sheet_index:
    :return:
    """
    data = xl.open_workbook(file_path)

    sheet = data.sheet_by_index(sheet_index)

    entities = []

    for i in range(1, len(sheet.col_values(2))):
        text = sheet.row_values(i)[0]  # the text which contains the annotation/s
        start = sheet.row_values(i)[2]  # start index of entity in text
        end = sheet.row_values(i)[3]  # end index of entity in text
        ent = sheet.row_values(i)[4]  # entity type

        # column 'text'
        if text != xl.empty_cell.value:
            # print(text)
            insert = (text, {"entities": [(int(start), int(end), ent)]})
            entities.append(insert)
        else:
            entities[-1][1]["entities"].append((int(start), int(end), ent))
    # print(entities)
    return entities

# Examples
# -------------------
# file = r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\output\train_data.txt"
#
# keyword = clipboard.paste()
# extract_index_pos(file, keyword, 0)

# file_path = r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\output\cv_68.txt"

# col1 = extract_information(file_path, 32, 73)
# col2 = extract_information(file_path, 73, 114)
# col3 = extract_information(file_path, 114, 155)
#
# print(*merge_list_strings(col1, col2, col3,ent="SKL", ent_col=1), sep=",\n")
