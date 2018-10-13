from sklearn.cluster import KMeans
import numpy as np

# standard KMeans
def get_hospitals(data):
    kmeans = KMeans(n_clusters=2).fit(data)
    print(kmeans.cluster_centers_)
    print(np.bincount(kmeans.labels_))

def custom_algo(data, K, maxIters = 100, fn=None):
    centroids = data[np.random.choice(np.arange(len(data)), K), :]
    if not fn:
        fn = lambda xd, yd: np.dot(xd, yd)
    for i in range(maxIters):
        # Cluster Assignment step
        C = np.array([np.argmin([fn(x_i-y_k, x_i-y_k) for y_k in centroids]) for x_i in data])
        # Move centroids step
        centroids = [data[C == k].mean(axis = 0) for k in range(K)]
    return np.array(centroids), C
    
if __name__ == "__main__":
    data = np.array([[1, 2], [1, 4], [1, 0], [4, 2], [4, 4], [4, 0]])
    get_hospitals(data)

    m1, cov1 = [9, 8], [[1.5, 2], [1, 2]]
    m2, cov2 = [5, 13], [[2.5, -1.5], [-1.5, 1.5]]
    m3, cov3 = [3, 7], [[0.25, 0.5], [-0.1, 0.5]]
    data1 = np.random.multivariate_normal(m1, cov1, 250)
    data2 = np.random.multivariate_normal(m2, cov2, 180)
    data3 = np.random.multivariate_normal(m3, cov3, 100)
    X = np.vstack((data1,np.vstack((data2,data3))))
    np.random.shuffle(X)
    centroids, C = custom_algo(X, K=3)
    print(centroids)
    print(C)
