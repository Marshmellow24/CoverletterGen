import spacy
import random
from datetime import datetime
from Data_Parsing import train_data_generator as tdg
from spacy.training.example import Example
import json


def convert_json_to_train_data(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        json_file = json.load(f)
    training_data = []

    for text in json_file:
        entities = []
        for ents in text["entities"]:
            entities.append(tuple(ents))
        training_data.append((text["content"], {"entities": entities}))

    return training_data


path = r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\output\output.json"

# Control index numbers: show annotated string
# for ent in TRAIN_DATA:
#     for item in ent[1]["entities"]:
#         # print(item[0])
#         print(ent[0][item[0]:item[1]])


def train_spacy(data, iterations):
    nlp = spacy.blank('de')  # create blank Language class

    # create the built-in pipeline components and add them to the pipeline
    if 'ner' not in nlp.pipe_names:
        nlp.add_pipe("ner", last=True)

    # get names of other pipes to disable them during training, actually not necessary since language class is blank
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe != 'ner']
    with nlp.disable_pipes(*other_pipes):  # only train NER
        optimizer = nlp.begin_training()
        for itn in range(iterations):
            print("Starting iteration " + str(itn))
            random.shuffle(data)
            losses = {}
            for texts, annotations in data:
                examples = [Example.from_dict(nlp.make_doc(texts), annotations) for texts, annotations in data]
                nlp.update(examples,
                           drop=0.5,  # dropout - make it harder to memorise data
                           sgd=optimizer,
                           losses=losses)
                print("Losses", losses)
    return nlp


# with open(r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\output\train_data_test.txt", "r", encoding="utf-8") as f:
#     buffer = f.readlines()
#
# for item in buffer:
#     train_data = [eval(item) for item in buffer]
# file = r"F:\UNI\Master WiInfo\Thesis\application\cb\bewerbungen_raw\output\train_data.xls"
#
# train_data = tdg.extract_from_xls(file)
# print(train_data)
#
# # Control index numbers: show annotated string
# for ent in train_data:
#     for item in ent[1]["entities"]:
#         # print(item[0])
#         print(ent[0][item[0]:item[1]])


start_time = datetime.now()

train_data = convert_json_to_train_data(path)

nernlp = train_spacy(train_data, 20)


nernlp.to_disk("cv_ents_json")
end_time = datetime.now()
print('Duration: {}'.format(end_time - start_time))