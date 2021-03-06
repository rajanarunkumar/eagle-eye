import boto3

s3 = boto3.resource('s3')

# Get list of objects for indexing
images = [('/Users/arunrajan/Downloads/IMG_0002.jpg', 'Spencer Wielgus')]

# Iterate through list to upload objects to S3
for image in images:
  file = open(image[0], 'rb')
  object = s3.Object('eagle-eye-validator',
                     'index/' + image[0].lstrip('/Users/arunrajan/Downloads/'))
  ret = object.put(Body=file, Metadata={'FullName': image[1]})
  print ret
