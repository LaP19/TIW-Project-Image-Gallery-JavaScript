{
	let personalMessage, albumsList, albumPictures, pictureDetails, othersAlbumList, createComment,
	  pageOrchestrator = new PageOrchestrator();
	  
	window.addEventListener("load" , () => {
		//If the user isn't logged in go back to the login page
        if(sessionStorage.getItem("username") == null){
            window.location.href = "index.html";
        }else{
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    } , false);
    
    //handles the creation of a new album
    document.getElementById("submitAlbum").addEventListener("click", (e) => {

		const form = e.target.closest("form");

        const title = form.querySelector("[name=title]").value;
        
        if (form.checkValidity() && title !== null && title !== "") {

            makeCall("POST", "CreateAlbum", form, function (request) {

                if (request.readyState === XMLHttpRequest.DONE) {
                    if(request.status === 200) {
						document.getElementById("saveChanges").style.display = "";
						pageOrchestrator.refresh();
						form.reset();
                    } else {
                        alert(request.responseText);
                    }
                }
            }, false);
        }else{
		    alert("In order to create it, the album title must not be empty");
		}
	})    
	
	/**
     * Handles the view of the personal message
     * @param username is the username to add in the HTML file
     * @param messageContainer is the place where the username has to be added
     */
    function PersonalMessage(username , messageContainer) {
        this.username = username;

        this.show = function() {
            messageContainer.textContent = this.username;
        }
    }
    
    /**
     * Handles the view of the albums owned by the session user
     * @param grid is the id of the div in which the albums have to be placed
     */
    function AlbumsList(grid) {
		
		//function that gets the albums from the server and calls the update in order to show them
		this.show = function(){
			
			var self = this;
			
			makeCall("GET", "GetAlbumsList", null,
			function(req){
				if(req.readyState == 4){
					if(req.status == 200){
						let albumsToShow = JSON.parse(req.responseText);

						if(albumsToShow.length == 0){
							albumPictures.reset();
		      				pictureDetails.reset();
							document.getElementById("firstTitle").textContent = "You don't have any album yet";
							document.getElementById("saveChanges").style.display = "none";
							return;
						}
						document.getElementById("firstTitle").textContent = "Here you can see your albums:";
						self.update(albumsToShow);
						
					}else if (req.status == 403) { //forbidden
					  //Redirect to index.html and remove the username from the session
	                  window.location.href = "index.html";
	                  window.sessionStorage.removeItem('username');
                  	}
	                 else {
		           		 alert(req.responseText);
		          	}
				}
			}, false);
		};
	
		this.update = function(arrayAlbums) {
			
		      var  albumCell, albumTitle, dateAlbum, anchor, linkText, newDiv, anchor2;
		      
		      //deletes the content of the grid
		      grid.innerHTML = "";
		      
		      var self = this;
		      
		      //For each album create its cell and make it draggable
		      arrayAlbums.forEach(function(album) {
		      	
		      	albumCell = document.createElement("div");
		      	albumCell.classList.add("cell");
		      	
		      	//Every cell of myAlbums must be draggable
				albumCell.setAttribute("draggable", true);
		      	albumTitle = document.createElement("span");
		      	albumTitle.classList.add("albumName");
		      	anchor = document.createElement("a");
		      	anchor.classList.add("anchor_album");
		      	anchor.setAttribute("album_id", album.id);
		      	albumTitle.appendChild(anchor);
		      	linkText = document.createTextNode(album.title);
		      	anchor.appendChild(linkText);
		      	anchor.addEventListener("click", () => {
				  albumPictures.reset();
				  document.getElementById("closePictures").style.display = "";
				  document.getElementById("titlePictures").textContent = "Pictures of Album: " + album.title;
				  document.getElementById("titlePictures").style.display = "";
				  document.getElementById("pictureDetailsPage").style.display = "none";
		          albumPictures.show(album.id);
		        });
		        anchor.href = "#titlePictures";
		        
		       	dateAlbum = document.createElement("span");
		      	dateAlbum.classList.add("date");
		      	dateAlbum.textContent = album.date;
		      
				
				newDiv = document.createElement("div");
				newDiv.classList.add("addPictures");
				anchor2 = document.createElement("a");
				anchor2.classList.add("buttonAdd");
		        newDiv.appendChild(anchor2);
				linkText = document.createTextNode("Add new pictures");
				anchor2.appendChild(linkText);
				anchor2.addEventListener("click", () => {
				  	document.getElementById("pictureDetailsPage").style.display = "none"
				  	document.getElementById("titlePictures").textContent = "Choose the pictures you want to add:";
				 	document.getElementById("titlePictures").style.display = "";
		           	albumPictures.showNoAlbumPictures(0, album.id);
		           	
		        });
		       	anchor2.href = "#titlePictures";

		        albumCell.appendChild(albumTitle);
		        albumCell.appendChild(dateAlbum);
		        albumCell.appendChild(newDiv);
		        
		        grid.appendChild(albumCell);
		    });
		    
		    //Dragging logic
		        
		        //Every cell must be draggable in the albumsList
		        draggables = document.querySelectorAll('.cell');
            	containers = document.querySelectorAll('.grid');
				
				//For every element contained in draggables add the event listener on drag start and assign the class dragging
            	draggables.forEach(draggable => {
	                draggable.addEventListener('dragstart', () => {
	                    draggable.classList.add('dragging')
	                })
				//Every draggable element has the listener on drag end so when the dragging has ended the class dragging is removed
	                draggable.addEventListener('dragend', () => {
	                    draggable.classList.remove('dragging')
	                })
           	    })
				
				//add the event dragover to the whole grid in order to know where the cell that has been dragged is being moved to
            	containers.forEach(container => {
	                container.addEventListener('dragover', e => {
						//Enables dropping
	                    e.preventDefault() //Otherwise the dropping is not allowed by default
	                    const afterElement = self.getDragAfterElement(container, e.clientY) //e.clientY is the y position of the mouse on the screen
	                    const draggable = document.querySelector('.dragging')  //the element that is being dragged (only one element has this selector at the same time)
	                    if (afterElement == null) { //if there is no element after, puts the cell at the end of the grid
	                        container.appendChild(draggable)
	                    } else { //if there is an element after, inserts the element that is being dragged before that element
	                        container.insertBefore(draggable, afterElement)
	                    }
	                    //resets the saveChanges button
	                    document.getElementById("saveChanges").removeAttribute("disabled");
	                    document.getElementById("saveChanges").removeEventListener("click", this.saveChangesOrder);
	                    //adds the event listener to the button
	                    document.getElementById("saveChanges").addEventListener("click", this.saveChangesOrder);
	                })
           		 })	        
			      
		      grid.style.visibility = "visible";
	
		};
		
		  //returns the element that is placed after the place in which another element is being dragged
		  this.getDragAfterElement = function(container, y) {
					
					//draggableElements is an array that contains all the elements of the grid but the one that is being dragged
	                const draggableElements = [...container.querySelectorAll('.cell:not(.dragging)')]
					
					//the function reduce loops through the array and determines which single element is the one directly after the cursor based on the y position
	                return draggableElements.reduce((closest, child) => {
	                    const box = child.getBoundingClientRect() //returns the smallest rectangle which contains the entire element
	                    const offset = y - box.top - box.height / 2 //remove half of the box height from the position of the cursor
	                    //when I'm above an element the offset is negative, so I only care about negative numbers
	                    if (offset < 0 && offset > closest.offset) {
	                        return { offset: offset, element: child };
	                    } else {
	                        return closest;
	                    }//initial value of reduce (the value of closest at the first iteration)
	                }, { offset: Number.NEGATIVE_INFINITY }).element 
	            }	
		
		this.saveChangesOrder = function () {
            let list = document.getElementById("grid");

            let albums = list.getElementsByClassName("anchor_album");
            let formData = [];
            
            let alb = Array.from(albums);
            let i;
            for (i = 0; i < alb.length; i++) {
                let data = alb[i].getAttribute("album_id");
                formData.push(data);
            }
            
            //call to the controller
            $.ajax({
                type: "POST",
                url: 'SaveOrder',
                data: JSON.stringify(formData),
                200: function () {
                    document.getElementById("saveChanges").setAttribute("disabled", true);
                },
                500: function (response) {
					alert(response.responseText);
                }
            });
        }

	    
    }
    
    /**
    * Handles the view and management of the pictures to show
     */
    function AlbumPictures(picturesData){
		this.picturesContainer = picturesData['picturesContainer']
		this.tableBody = picturesData['tableBody'];
		
		this.reset = function(){
				this.picturesContainer.style.display = "none";
				pictureDetailsPage.style.display = "none";
				document.getElementById("closeButton").style.display = "none";				
				document.getElementById("closePictures").style.display = "none";
				document.getElementById("titlePictures").style.display = "none";
		}
		
		this.show = function(albumId){
			var self = this;
			
			document.getElementById("closePictures").addEventListener("click", () => {
				self.reset();
			})
			
			makeCall("GET", "GetPictures?albumId=" + albumId, null,
			function(req){
				if(req.readyState == 4){
					if(req.status == 200){
						let picturesOfAlbum = JSON.parse(req.responseText);
						if(picturesOfAlbum.length == 0){
							document.getElementById("closePictures").style.display = "";
                            document.getElementById("no_pictures").textContent = "No pictures yet";
                            document.getElementById("no_pictures").style.display = "";
                            return;
                        }
						
						document.getElementById("no_pictures").style.display = "none";
						document.getElementById("closePictures").style.display = "";
						self.update(0, picturesOfAlbum, albumId);
						
                  	}else if (req.status == 403) { //forbidden
					  //Redirect to index.html and remove the username from the session
	                  window.location.href = "index.html";
	                  window.sessionStorage.removeItem('username');
                  	}else {
	              		alert(req.responseText)
	              	}
				}
			})
		};
		
		//Called when the user wants to add pictures
		this.showNoAlbumPictures = function(section, albumId){
			var self = this;
			
			document.getElementById("closeButton").style.display = "none";
			
			document.getElementById("closePictures").addEventListener("click", () => {
				self.reset();
				document.getElementById("no_pictures").style.display = "none";
			})
			
			makeCall("GET", "ShowPicturesToAdd", null,
			function(req){
				if(req.readyState == 4){
					if(req.status == 200){
						let picturesOfAlbum = JSON.parse(req.responseText);
						if(picturesOfAlbum.length == 0){
							self.reset();
                            document.getElementById("no_pictures").textContent = "There are no more pictures";
                            document.getElementById("no_pictures").style.display = "";
                            return;
                        }
						
						document.getElementById("no_pictures").style.display = "none";
						self.update(section, picturesOfAlbum, albumId);
					}else if (req.status == 403) { //forbidden
					  //Redirect to index.html and remove the username from the session
	                  window.location.href = "index.html";
	                  window.sessionStorage.removeItem('username');
                  	}else {
	              		alert(req.responseText);
	              	}
				}
			})
		};

	    this.update = function(section, pictures, albumId) {
	      var row1, row2, titleCell, anchor, pictureCell, previousButton, nextButton;
	       
		      this.tableBody.innerHTML = ""; // empty the table body

		      var self = this;
		      let next = false;
		      let previous = false;
		      
		      //The secion is used to know which group of five pictures to show
		      if(section < 0 || !section){
				section = 0;
			  }
			  
			  if(section + 5 < pictures.length){
				next = true;
			  }
			  
			  if(section - 5 >= 0){
				previous = true;
			  } 
			  
			  let picturesToShow;

              if (pictures.length >= section + 5){
            	picturesToShow = pictures.slice(section , section + 5);
              }   
               
              else{
            	picturesToShow = pictures.slice(section, pictures.length);
              }
              
			  
		      row1 = document.createElement("tr");
		      row2 = document.createElement("tr");
		      
		      //previous button
		      if(previous){
						previousButton = document.createElement("td");
						var button = document.createElement("button");
						button.textContent = "Precedent";
						button.classList.add("successive_precedent");
						previousButton.appendChild(button);
						var blank = document.createElement("td");
						blank.textContent = "";
						button.addEventListener ("click", () => {
		          			self.update(section - 5, pictures, albumId);
						});
				//Clones the node in order to remove the listeners previously attached			
				let newButton = document.getElementById('submitComment');
				newButton.replaceWith(newButton.cloneNode(true));
				row1.appendChild(previousButton);
				row2.appendChild(blank);
			  }

		      picturesToShow.forEach(function(picture) { // self visible here, not this
			      	
			      	pictureCell = document.createElement("td");
			      	var img = document.createElement("img");
	  				img.src = picture.path;
	  				img.style.height = "180px";
	  				img.style.width = "260px";
	  				img.classList.add("thumbnail");
	  				anchor = document.createElement("a");
	  				anchor.appendChild(img);
	  				pictureCell.appendChild(anchor);
	  				
	  				//if the method has been called in order to see the pictures of an album
	  				if(picture.album != null){
				        anchor.addEventListener("mousemove", () => {
						  //Clones the node in order to remove the listeners previously attached	
						  var newButton = document.getElementById('submitComment');
						  newButton.replaceWith(newButton.cloneNode(true));
						  //calls the method that allows to create a comment
				          createComment.commentCreatorFunction(picture);
				          document.getElementById("comment").value = "";
				          pictureDetails.show(picture);
				        });
				        anchor.href = "#";
			        } 
			        //If the method has been called in order to add pictures to an album
			        else {
						var newButton = document.getElementById('submitComment');
						  newButton.replaceWith(newButton.cloneNode(true));
						  document.getElementById('closePictures').style.display = "";
						  
						  anchor.addEventListener("click", () => {
					          makeCall("POST", "AddPictureToAlbum?albumid=" + albumId + "&pictureid=" + picture.id, null, function (request) {
	
				                if (request.readyState === XMLHttpRequest.DONE) {
				                    if(request.status === 200) {
										self.showNoAlbumPictures(section, albumId);
				                    } else if (req.status == 403) { //forbidden
									  //Redirect to index.html and remove the username from the session
					                  window.location.href = "index.html";
					                  window.sessionStorage.removeItem('username');
				                  	}else {
								       alert(request.responseText);
				                    }
	                		}
                		});
            		});
				          
					}
	  				
	  				row1.appendChild(pictureCell);
			        titleCell = document.createElement("td");
			        titleCell.textContent = picture.title;			        
			        row2.appendChild(titleCell);
			        
				    });
				    
			  //next button	    
			  if(next){
						nextButton = document.createElement("td");
						var button = document.createElement("button");
						button.textContent = "Successive";
						button.classList.add("successive_precedent");
						nextButton.appendChild(button);
						var blank = document.createElement("td");
						blank.textContent = "";
						button.addEventListener ("click", () => {
		          			self.update(section + 5, pictures, albumId);
						});
				
				//Clones the node in order to remove the listeners previously attached		
				var newButton = document.getElementById('submitComment');
				newButton.replaceWith(newButton.cloneNode(true));
				row1.appendChild(nextButton);
				row2.appendChild(blank);
			  }
				    
	    	  self.tableBody.appendChild(row1);
	    	  self.tableBody.appendChild(row2);
			      
			  this.picturesContainer.style.display = "";
	    };	    
}

	
    /**
    * Handles the view and management of the pictures to show
    *@param alertNoComments id of the div in which the alert of no comments available is shown
    *@param pictureDetailsPage the main div od the picture details
    *@param pictureDetailsBody the body of the picture details display
    *@param commentsTable the grid in which the comments related to the picture are displayed
     */
	function PictureDetails(alertNoComments, pictureDetailsPage, pictureDetailsBody, commentsTable){		
	
		this.reset = function() {
            pictureDetailsPage.style.display = "none";
            pictureDetailsBody.style.display = "none";
            document.getElementById("closeButton").style.display = "none";
        }
        
        this.show = function(picture) {
	
           var self = this;
           
           document.getElementById("modal").style.display = "block";           
           self.update(picture)
           
			makeCall("GET", "GetPictureDetails?pictureid=" + picture.id, null,
			function(req){
				if(req.readyState == 4){
					if(req.status == 200){
						let comments = JSON.parse(req.responseText);
						if(Object.keys(comments).length == 0){
							self.updateNoComments();
							return;
						} else{
							self.updateComments(comments);
						}
					}else if (req.status == 403) { //forbidden
					  //Redirect to index.html and remove the username from the session
	                  window.location.href = "index.html";
	                  window.sessionStorage.removeItem('username');
                  	}
                  	else {
	           		 alert(req.responseText);
	          		}
				}
			});
   		 }

        this.update = function(picture) {
	
			let titleCell, dateCell, descriptionCell, img, span;
			
            pictureDetailsBody.innerHTML = "";

            titleCell = document.createElement("h2");
            titleCell.classList.add("title");
			titleCell.textContent = picture.title;
            pictureDetailsBody.appendChild(titleCell);
            
            span = document.createElement("span");
            img = document.createElement("img");
	  		img.src = picture.path;
	  		img.classList.add("image");
	  		span.appendChild(img)
	  		pictureDetailsBody.appendChild(span);

            dateCell = document.createElement("p");
			dateCell.textContent = picture.date;
            pictureDetailsBody.appendChild(dateCell);
            
            descriptionCell = document.createElement("p");
			descriptionCell.textContent = picture.description;
            pictureDetailsBody.appendChild(descriptionCell);
            
			document.getElementById("closeButton").addEventListener("click", () => {
				document.getElementById("modal").style.display = "none";
				document.getElementById("closeButton").style.display = "none";
			})

			document.getElementById("closeButton").style.display = "";
			
			pictureDetailsBody.style.display = "";
            pictureDetailsPage.style.display = "";
            
        }
        
        this.updateComments = function(comments){
	
			let commentText;
	
			commentsTable.innerHTML = "";
			
			document.getElementById("comments_title").style.display = "";
			
			Object.keys(comments).forEach(function(commentKey) {
				
			    for(var i = 0; i < comments[commentKey].length; i++){
					
					idCreator = document.createElement("div");
				    idCreator.textContent = commentKey;
				    idCreator.classList.add("column");
					
					commentText = document.createElement("div");
					commentText.textContent = comments[commentKey][i];
					commentText.classList.add("column");
					
					commentsTable.appendChild(idCreator);
					commentsTable.appendChild(commentText);
				}

				commentsTable.style.display = "";
				
			});
			
			alertNoComments.style.visibility = "hidden";
		
		}
		
		this.updateNoComments = function(){

			document.getElementById("comments_title").style.display = "none";
			alertNoComments.style.visibility = "visible";
			commentsTable.style.display = "none";
			alertNoComments.textContent = "There are no comments yet!";
		}
	}
	
	/** 
	* Handles the textArea and button that allow the user to create a new comment
	*/
	function CommentCreator(){
		
		this.commentCreatorFunction = function(picture){
			document.getElementById("submitComment").addEventListener("click" , (e) => {
				
		       	let form = e.target.closest("form");
		
		        if (form.checkValidity()) {
			
					let comment = document.getElementById("comment").value;
					
					if(this.isAGoodComment(comment)){
			            makeCall("POST", "CreateComment?pictureid=" + picture.id, form, function (request) {
			
			                if (request.readyState === XMLHttpRequest.DONE) {
			                    if(request.status == 200) {
									pictureDetails.show(picture);
									form.reset();
			                    }else if (req.status == 403) { //forbidden
								  //Redirect to index.html and remove the username from the session
				                  window.location.href = "index.html";
				                  window.sessionStorage.removeItem('username');
			                  	} else {
			                        alert(request.responseText);
			                    }
			                }
			            });
		            }
		        } else{
					alert("In order to submit it, the comment must not be empty");
				}
		        });
        }
	     
	    //Cheks if the comment is not empty   
        this.isAGoodComment = function(comment) {
	
	        if(comment != "" && comment != null && comment.trim() != ""){
	            return true;}
	        else {
	            alert("In order to submit it, the comment must not be empty");
	            return false;
	        }
    	}
	}
	
	
	 /**
     * Handles the view of the albums owned the other users
     * @param gridOthers is the id of the div in which the albums have to be placed
     */
	function OthersAlbumList(gridOthers) {
        
        this.reset = function(){
			gridOthers.style.visibility = "hidden";
		}
		
		this.show = function(){
			
			var self = this;
			
			makeCall("GET", "GetOthersAlbums", null,
			function(req){
				//State 4 : the request had been sent, the server has finished returning the response and the browser 
				//has finished downloading the response content. => the call has been completed
				if(req.readyState == 4){
					if(req.status == 200){
						let albums = JSON.parse(req.responseText);
						if(Object.keys(albums).length == 0){
							document.getElementById("titleOtherUsers").textContent = "There isn't any album created by other users yet"
							return;
						}
						self.update(albums);
					}else if (req.status == 403) { //forbidden
					  //Redirect to index.html and remove the username from the session
	                  window.location.href = "index.html";
	                  window.sessionStorage.removeItem('username');
                  	}
                  else {
	           		 alert(req.responseText);
	          		}
				}
			});
		};
	
		this.update = function(albumsMap) {
			var  albumCell, albumTitle, dateAlbum, anchor, linkText, usernameSpan, newDiv, newSpan;
		      gridOthers.innerHTML = "";
		      
		      Object.keys(albumsMap).forEach(function(key) { 
				
				let i = 1;
				let id = "";
				let albumName = "";
				let date = "";
				
				//Parses the id
				while (key[i] != ","){
					id = id.concat(key[i]);
					i++;
				}
				
				i++;
				
				//Parses the album name
				while (key[i] != ","){
					albumName = albumName.concat(key[i]);
					i++;
				}
				
				i++;
				
				//Parses the date
				while (key[i] != "]"){
					date = date.concat(key[i]);
					i++;
				}

				
				albumCell = document.createElement("div");
		      	albumCell.classList.add("cellOthers");
		      	
		      	newDiv = document.createElement("div");
		      	
				usernameSpan = document.createElement("span");
				usernameSpan.classList.add("username");
				usernameSpan.textContent = albumsMap[key];
				
				dateAlbum = document.createElement("span");
		      	dateAlbum.classList.add("dateOthers");
		      	dateAlbum.textContent = date;
		      	
		      	newDiv.appendChild(usernameSpan);
		      	newDiv.appendChild(dateAlbum);
		      	
		      	albumTitle = document.createElement("div");
		      	newSpan = document.createElement("span");
		      	albumTitle.appendChild(newSpan);
		      	newSpan.classList.add("albumNameOthers");
		      	anchor = document.createElement("a");
		      	newSpan.appendChild(anchor);
		      	linkText = document.createTextNode(albumName);
		      	anchor.appendChild(linkText);
		      	
		      	anchor.addEventListener("click", () => {
				  albumPictures.reset();
				  document.getElementById("pictureDetailsPage").style.display = "none";
				  document.getElementById("titlePictures").textContent = "Pictures of Album: " + albumName;
				  document.getElementById("titlePictures").style.display = "";
				  document.getElementById("pictureDetailsPage").style.display = "none";
		          albumPictures.show(id);
		        });
		        
		        anchor.href = "#titlePictures";
				
				albumCell.appendChild(newDiv);
		        albumCell.appendChild(albumTitle);
		        
		        gridOthers.appendChild(albumCell);
		    });
			      
		      gridOthers.style.visibility = "visible";
	
		};
	}
    
	
	
    /**
     * The main controller of the application
     */
    function PageOrchestrator(){
	    
	    this.start = function() {
		
		  //sets the username for the "nice to see you again" message
	      	personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
	        document.getElementById("id_username"));
			
			//contains the function to display the albums
	      	albumsList = new AlbumsList(document.getElementById("grid"));
		        
		    //contains the function to display the pcitures
	    	albumPictures = new AlbumPictures({
				picturesContainer: document.getElementById("id_picturesContainer"),
				tableBody: document.getElementById("id_pictureTableBody")
				});
				
			//Set the event of logout to the anchor
			document.querySelector("a[href='Logout']").addEventListener('click', () => {
	                window.sessionStorage.removeItem('username');
	         });
		
		    pictureDetails = new PictureDetails(document.getElementById("alertNoComments"), document.getElementById("pictureDetailsPage") , document.getElementById("pictureDetailsTable"), 
		    document.getElementById("commentsTable"));
        
	        createComment = new CommentCreator();
	        
	        othersAlbumList = new OthersAlbumList(document.getElementById("gridOthers"));

        };
        
        this.refresh = function() {
	      personalMessage.show();
	      albumsList.show();
	      othersAlbumList.show();
	      albumPictures.reset();
		  pictureDetails.reset();
	    };
	}	

}
        