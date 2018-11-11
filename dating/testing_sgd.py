import numpy as np
from sklearn import linear_model, ensemble
import random

def generateTarget(n_features=30):
    target = [0.0] * n_features
    pos = 100
    neg = 100
    while pos != 0 or neg != 0:
        ind = random.choice([ind for ind in range(n_features) if target[ind] == 0.0])
        if random.randint(1, 2) == 1:
            partial = random.randint(0, pos)
            target[ind] = partial / 100.0
            pos -= partial
        else:
            partial = random.randint(0, neg)
            target[ind] = partial * -1 / 100.0
            neg -= partial
    return target

def generateRandomTest(n_features=30, n_samples=40):
    X = np.random.sample(size=(n_samples, n_features))
    Xn = np.round(X, decimals=2)
    return Xn

def modifyTarget(target):
    n_features = np.shape(target)[0]
    max_changed = n_features // 20
    max_percent = .2
    ind_to_change = np.random.choice([i for i in range(n_features)], size=random.randint(0, max_changed), replace=False)
    new_target = []
    for i, v in enumerate(target):
        if i in ind_to_change:
            new_target.append(v * (1 + max_percent))
        else:
            new_target.append(v)
    return new_target

if __name__ == "__main__":

    MAX_ITERATIONS = 20
    n_features, n_samples = 200, 40

    # Generate random tests and target date
    target = generateTarget(n_features=n_features)
    X = generateRandomTest(n_features=n_features, n_samples=n_samples)
    y = np.dot(X, target)

    best_all = -1
    best_w_all = None
    for i in range(1, MAX_ITERATIONS + 1):

        # Get regressor
        clf = linear_model.Ridge(alpha=.0001)
        # clf = linear_model.SGDRegressor(n_iter=1000)
        clf.fit(X, y)

        # Get positive weights found by the regressor
        test_w = []
        for co in clf.coef_:
            if co > 0:
                test_w.append(1)
            else:
                test_w.append(0)

        # a = np.zeros((n_features, n_features), int)
        # np.fill_diagonal(a, 1)
        # test_y =  clf.predict(a)
        # for score in test_y:
            # if score > 0:
                # test_w.append(1)
            # else:
                # test_w.append(0)

        print("Best Vector found by regresser:", test_w)
        test_score = np.dot(test_w, modifyTarget(target))
        test_score = max(np.dot(test_w, target), test_score)

        print("Actual Score:", test_score)

        # add test vector and its actual score to the training data
        print("Adding to regressor...")
        np.append(X, test_w)
        np.append(y, test_score)

        if best_w_all is None or test_score > best_all:
            best_w_all = test_w
            best_all = test_score

    print("Best Score Over all Iterations:", best_all)
    print("Best Vector Over all Iterations:", best_w_all)
