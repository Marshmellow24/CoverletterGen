import logging
import os
import sys
import time

import torch
from strsimpy.damerau import Damerau
from simpletransformers.t5 import T5Model


damerau = Damerau()


def readFile(path):
    result = []
    with open(path, "r", encoding="utf-8") as f:
        input = f.readlines()
    text = "".join(input)
    # splits cover letter into paragraphs divided by empty lines
    temp = text.split("\n\n")
    for paragraph in temp:
        # splits paragraphs with more than 400 chars 
        result.extend(paragraph_splitter(paragraph))
    return result


def paragraph_splitter(paragraph):
    out = []
    if len(paragraph) > 400:
        # split by sentence, maximum of 2 times
        split_paragraphs = paragraph.split(". ", maxsplit=2)
        # add dot at end of sentece if its not the last one
        for sentence in split_paragraphs:
            if not sentence == split_paragraphs[-1]:
                sentence = sentence + "."
                out.append(sentence)
            # if last sentence still too long split until not    
            elif len(split_paragraphs[-1]) > 400:
                out.extend(paragraph_splitter(split_paragraphs[-1]))
            else:
                out.append(sentence)
    # return paragraphs that are not too long w/o split
    else:
        out.append(paragraph)
    return out

    
def paraphrase(text, cuda=False):
    logging.basicConfig(level=logging.INFO)
    transformers_logger = logging.getLogger("transformers")
    transformers_logger.setLevel(logging.ERROR)

    model = T5Model(model_type="t5", model_name="outputs", use_cuda=cuda)
    output = []
    # predicts paraphrases for every paragraph
    for paragraph in text:
        # add prefix 
        predict_to = ["paraphrase: " + paragraph]

        preds = model.predict(predict_to)

        print("---------------------------------------------------------")
    
        print("Predictions >>>")
        result = []
        # appends damerau distance to every prediction and compares it with original
        for pred in preds[0]:
            result.append([pred, damerau.distance(paragraph, pred)])
            # result.append([pred, jarowinkler.distance(paragraph, pred)])
            print(pred)
            print(damerau.distance(paragraph, pred))
        # picks the most diversified prediction
        print("---------------------------------------------------------")
        best_pred = max(result, key=lambda x: x[1])[0]
        output.append(best_pred)
        print(best_pred)
    # outputs the total damerau distance and the paraphrased text
    print(*output, sep="\n")
    print("Diversified by: ", damerau.distance("".join(text), "".join(output)))
    return output


def writeToFile(paraphrased):
    dir_path = os.path.dirname(__file__)
    output_path = os.path.join(dir_path, "../../../data/coverletters/")
    timestr = time.strftime("%Y%m%d_%H%M%S")
    # writes paraphrased text to file
    with open(output_path + r"coverletter" + timestr + r".txt", "wt", encoding="utf-8") as f:
        f.write("\n\n".join(paraphrased))
    return r"coverletter" + timestr + r".txt"


if __name__ == "__main__":
    pathFile = sys.argv[1]
    if sys.argv[2] == "true":
        cuda_switch = True
    elif sys.argv[2] == "false":
        cuda_switch = False
    else:
        raise ValueError("[ERROR] False value, make sure you specify boolean value for cuda argument")
    torch.multiprocessing.freeze_support()
    print(writeToFile(paraphrase(readFile(pathFile), cuda_switch)))
